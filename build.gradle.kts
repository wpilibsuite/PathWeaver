plugins {
    `java`
    `application`
    `pmd`
    `checkstyle`
}

group = "edu.wpi.first.path_ui"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile("org.apache.commons", "commons-csv", "1.5")
    compile("javax.measure", "unit-api", "1.0");
    compile("si.uom", "si-units", "0.9");
    compile("systems.uom", "systems-common", "0.8");
    compile("org.json","json","20180130");

}

checkstyle {
    toolVersion = "8.10"
    if (project.hasProperty("ignoreCheckstyle")) {
        isIgnoreFailures = true
    }
}

pmd {
    isConsoleOutput = true
    toolVersion = "6.3.0"
    reportsDir = file("${project.buildDir}/reports/pmd")
    ruleSetFiles = files(File(rootDir, "pmd-ruleset.xml"))
    ruleSets = emptyList()
}

application {
    mainClassName = "edu.wpi.first.pathui.PathUI"
}

task<Wrapper>("wrapper") {
    gradleVersion = "4.7"
    distributionType = Wrapper.DistributionType.ALL
}
