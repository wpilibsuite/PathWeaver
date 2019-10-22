import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.time.Instant
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import groovy.lang.GroovyObject

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
}
plugins {
    `maven-publish`
    jacoco
    java
    pmd
    application
    id("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin") version "4.0.1"
    id("edu.wpi.first.wpilib.repositories.WPILibRepositoriesPlugin") version "2020.1"
    id("com.jfrog.artifactory") version "4.9.8"
    id("com.github.johnrengelman.shadow") version "4.0.3"
    id("com.diffplug.gradle.spotless") version "3.25.0"
}

if (hasProperty("buildServer")) {
    wpilibVersioning.isBuildServerMode = true
}

if (hasProperty("releaseMode")) {
    wpilibVersioning.isReleaseMode = true
}

repositories {
    mavenCentral()
}

if (hasProperty("releaseMode")) {
    wpilibRepositories.addAllReleaseRepositories(project)
} else {
    wpilibRepositories.addAllDevelopmentRepositories(project)
}

repositories {
    maven {
        url = uri("https://dev.imjac.in/maven/")
    }
    maven {
        url = uri("https://first.wpi.edu/FRC/roborio/maven/release/")
    }
}

wpilibVersioning.version.finalizeValue()
version = wpilibVersioning.version.get()

if (System.getenv()["RUN_AZURE_ARTIFACTORY_RELEASE"] != null) {
    artifactory {
        setContextUrl("https://frcmaven.wpi.edu/artifactory") // base artifactory url
        publish(delegateClosureOf<PublisherConfig> {
            repository(delegateClosureOf<GroovyObject> {
                if (project.hasProperty("releaseMode")) {
                    setProperty("repoKey", "release")
                } else {
                    setProperty("repoKey", "development")
                }
                setProperty("username", System.getenv()["ARTIFACTORY_PUBLISH_USERNAME"])
                setProperty("password", System.getenv()["ARTIFACTORY_PUBLISH_PASSWORD"])
                setProperty("maven", true)
            })
            defaults(delegateClosureOf<GroovyObject> {
                invokeMethod("publications", "app")
            })
        })
        clientConfig.info.buildName = "PathWeaver"
    }

    tasks.named("publish") {
        dependsOn(tasks.named("artifactoryPublish"))
    }
}

val theMainClassName = "edu.wpi.first.pathweaver.Main"

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Implementation-Version"] = project.version as String
        attributes["Built-Date"] = Instant.now().toString()
        attributes["Main-Class"] = theMainClassName
    }
}

application {
    mainClassName = theMainClassName
}

// Spotless is used to lint and reformat source files.
spotless {
    java {
        removeUnusedImports()
    }
    kotlinGradle {
        ktlint("0.33.0")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    format("extraneous") {
        target("Dockerfile", "*.sh", "*.yml")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}

createNativeConfigurations()

dependencies {
    javafx(name = "base")
    javafx(name = "controls")
    javafx(name = "fxml")
    javafx(name = "graphics")
    compile(group = "org.fxmisc.easybind", name = "easybind", version = "1.0.3")

    compile(group = "javax.measure", name = "unit-api", version = "1.0")
    compile(group = "si.uom", name = "si-units", version = "0.9")
    compile(group = "systems.uom", name = "systems-common", version = "0.8")

    compile(group = "com.google.code.gson", name = "gson", version = "2.8.5")
    compile(group = "org.apache.commons", name = "commons-csv", version = "1.5")

    compile(group = "org.ejml", name = "ejml-simple", version = "0.38")
    compile(group = "edu.wpi.first.wpiutil", name = "wpiutil-java", version = "2020.+")
    compile(group = "edu.wpi.first.wpilibj", name = "wpilibj-java", version = "2020.+")

    fun junitJupiter(name: String, version: String = "5.2.0") =
        create(group = "org.junit.jupiter", name = name, version = version)
    fun testFx(name: String, version: String = "4.0.13-alpha") =
        create(group = "org.testfx", name = name, version = version)

    testCompile(junitJupiter(name = "junit-jupiter-api"))
    testCompile(junitJupiter(name = "junit-jupiter-engine"))
    testCompile(junitJupiter(name = "junit-jupiter-params"))
    testCompile(group = "com.google.guava", name = "guava-testlib", version = "23.0")
    testCompile(testFx(name = "testfx-core"))
    testCompile(testFx(name = "testfx-junit5"))

    testRuntime(testFx(name = "openjfx-monocle", version = "jdk-9+181"))
    testRuntime(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.0.0")
}

tasks.withType<JavaCompile>().configureEach {
    // UTF-8 characters are used in menus
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
}

jacoco {
    toolVersion = "0.8.2"
}

extensions.getByType<PmdExtension>().apply {
    toolVersion = "6.8.0"
    isConsoleOutput = true
    reportsDir = file("${project.buildDir}/reports/pmd")
    ruleSetFiles = files(file("$rootDir/pmd-ruleset.xml"))
    ruleSets = emptyList()
}

tasks.withType<JacocoReport>().configureEach {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}

tasks.withType<Test>().configureEach {
    // TODO: re-enable when TestFX (or the underlying JavaFX problem) is fixed
    println("UI tests will not be run due to TestFX being broken when headless on Java 10.")
    println("See: https://github.com/javafxports/openjdk-jfx/issues/66")
    // Link: https://github.com/javafxports/openjdk-jfx/issues/66
    useJUnitPlatform {
        excludeTags("UI")
    }
}

tasks.withType<Javadoc>().configureEach {
    isFailOnError = false
}

val nativeShadowTasks = NativePlatforms.values().map { platform ->
    tasks.create<ShadowJar>("shadowJar-${platform.platformName}") {
        classifier = platform.platformName
        configurations = listOf(
                project.configurations.getByName("compile"),
                project.configurations.getByName(platform.platformName)
        )
        from(
                project.sourceSets["main"].output
        )
    }
}

tasks.create("shadowJarAllPlatforms") {
    nativeShadowTasks.forEach {
        this.dependsOn(it)
    }
}

tasks.withType<ShadowJar>().configureEach {
    exclude("module-info.class")
}

publishing {
    publications {
        create<MavenPublication>("app") {
            groupId = "edu.wpi.first.wpilib"
            artifactId = "PathWeaver"
            version = project.version as String
            nativeShadowTasks.forEach {
                artifact(it) {
                    classifier = it.classifier
                }
            }
        }
    }
}

tasks.withType<Wrapper>().configureEach {
    gradleVersion = "5.0"
}
