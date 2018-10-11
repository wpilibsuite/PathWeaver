import edu.wpi.first.wpilib.versioning.ReleaseType
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.time.Instant
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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
    checkstyle
    application
    pmd
    id("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin") version "2.2"
    id("com.github.johnrengelman.shadow") version "2.0.1"
    id("com.diffplug.gradle.spotless") version "3.13.0"
}

// Ensure that the WPILibVersioningPlugin is setup by setting the release type, if releaseType wasn't
// already specified on the command line
if (!hasProperty("releaseType")) {
    WPILibVersion {
        releaseType = ReleaseType.DEV
    }
}

version = getWPILibVersion()

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

repositories {
    mavenCentral()
}

// Spotless is used to lint and reformat source files.
spotless {
    kotlinGradle {
        ktlint("0.24.0")
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
    // JavaFX dependencies
    javafx("base")
    javafx("controls")
    javafx("fxml")
    javafx("graphics")

    compile("org.apache.commons", "commons-csv", "1.5")
    compile("javax.measure", "unit-api", "1.0")
    compile("si.uom", "si-units", "0.9")
    compile("systems.uom", "systems-common", "0.8")
    compile("com.google.code.gson", "gson", "2.8.5")
    compile(files("lib/Pathfinder-Java.jar"))

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

checkstyle {
    toolVersion = "8.12"
}

pmd {
    toolVersion = "6.8.0"
    isConsoleOutput = true
    reportsDir = file("${project.buildDir}/reports/pmd")
    ruleSetFiles = files(file("$rootDir/pmd-ruleset.xml"))
    ruleSets = emptyList()
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
                project.configurations.compile,
                project.configurations.getByName(platform.platformName)
        )
        from(
                java.sourceSets["main"].output
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

val sourceJar = task<Jar>("sourceJar") {
    description = "Creates a JAR that contains the source code."
    from(java.sourceSets["main"].allSource)
    classifier = "sources"
}

val javadocJar = task<Jar>("javadocJar") {
    dependsOn("javadoc")
    description = "Creates a JAR that contains the javadocs."
    from(java.docsDir)
    classifier = "javadoc"
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

/**
 * @return publishVersion property if exists, otherwise
 * [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or fallback
 * if that value is the empty string.
 */
fun getWPILibVersion(fallback: String = "0.0.0"): String {
    if (project.hasProperty("publishVersion")) {
        val publishVersion: String by project
        return publishVersion
    } else if (WPILibVersion.version != "") {
        return WPILibVersion.version
    } else {
        return fallback
    }
}

tasks.withType<Wrapper>().configureEach {
    gradleVersion = "4.9"
}

/**
 * Retrieves the [java][org.gradle.api.plugins.JavaPluginConvention] project convention.
 */
val Project.`java`: org.gradle.api.plugins.JavaPluginConvention
    get() =
        convention.getPluginByName("java")

/**
 * Retrieves the [checkstyle][org.gradle.api.plugins.quality.CheckstyleExtension] project extension.
 */
val Project.`checkstyle`: org.gradle.api.plugins.quality.CheckstyleExtension
    get() =
        extensions.getByName("checkstyle") as org.gradle.api.plugins.quality.CheckstyleExtension

/**
 * Configures the [checkstyle][org.gradle.api.plugins.quality.CheckstyleExtension] project extension.
 */
fun Project.`checkstyle`(configure: org.gradle.api.plugins.quality.CheckstyleExtension.() -> Unit) =
        extensions.configure("checkstyle", configure)

/**
 * Retrieves the [pmd][org.gradle.api.plugins.quality.PmdExtension] project extension.
 */
val Project.`pmd`: org.gradle.api.plugins.quality.PmdExtension
    get() =
        extensions.getByName("pmd") as org.gradle.api.plugins.quality.PmdExtension

/**
 * Configures the [pmd][org.gradle.api.plugins.quality.PmdExtension] project extension.
 */
fun Project.`pmd`(configure: org.gradle.api.plugins.quality.PmdExtension.() -> Unit) =
        extensions.configure("pmd", configure)
