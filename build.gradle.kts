import java.util.*

plugins {
    id("com.github.johnrengelman.shadow")
    id("com.palantir.graal")
    id("com.apollographql.apollo")
    id("com.avast.gradle.docker-compose")
    kotlin("jvm")
}

val cliGroup: String by project
val cliVersion: String by project
val jacksonVersion: String by project
val apolloVersion: String by project
val commonsLang3Version: String by project
val cliktVersion: String by project
val bouncycastleVersion: String by project
val jupiterVersion: String by project
val commonsIoVersion: String by project
val auth0JwtVersion: String by project
val protocolVersion: String by project
val vertxVersion: String by project

group = cliGroup
version = cliVersion

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io") { name = "jitpack" }
}

dependencies {
    implementation("com.apollographql.apollo:apollo-runtime:$apolloVersion")
    implementation("com.apollographql.apollo:apollo-coroutines-support:$apolloVersion")
    api("com.apollographql.apollo:apollo-api:$apolloVersion")

    implementation("com.github.sourceplusplus.protocol:protocol:$protocolVersion")

    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
    implementation("org.bouncycastle:bcprov-jdk15on:$bouncycastleVersion")
    implementation("org.bouncycastle:bcpkix-jdk15on:$bouncycastleVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("commons-io:commons-io:$commonsIoVersion")
    implementation("com.auth0:java-jwt:$auth0JwtVersion")
    implementation("eu.geekplace.javapinning:java-pinning-core:1.2.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
}

//todo: shouldn't need to put in src (github actions needs for some reason)
tasks.create("createProperties") {
    if (System.getProperty("build.profile") == "release") {
        val buildBuildFile = File(projectDir, "src/main/resources/build.properties")
        if (buildBuildFile.exists()) {
            buildBuildFile.delete()
        } else {
            buildBuildFile.parentFile.mkdirs()
        }

        buildBuildFile.writer().use {
            val p = Properties()
            p["build_id"] = UUID.randomUUID().toString()
            p["build_date"] = Date().toInstant().toString()
            p["build_version"] = project.version.toString()
            p.store(it, null)
        }
    }
}
tasks["processResources"].dependsOn("createProperties")

graal {
    //graalVersion(graalVersion.toString())
    mainClass("spp.cli.Main")
    outputName("spp-cli")
    option("-H:+PrintClassInitialization")
    option("-H:+ReportExceptionStackTraces")
    option("-H:IncludeResourceBundles=build")
    option("-H:+AddAllCharsets")
    javaVersion("11")
}

tasks.getByName<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("spp-cli")
    archiveClassifier.set("")
    manifest {
        attributes(
            mapOf(
                "Main-Class" to "spp.cli.Main"
            )
        )
    }
}
tasks.getByName("build").dependsOn("shadowJar")

configurations.runtimeClasspath {
    exclude("ch.qos.logback", "logback-classic")
    exclude("org.slf4j", "slf4j-api")
}

tasks.getByName<Test>("test") {
    failFast = true
    useJUnitPlatform()
    if (System.getProperty("test.profile") != "integration") {
        exclude("integration/**")
    }

    testLogging {
        events("passed", "skipped", "failed")
        setExceptionFormat("full")

        outputs.upToDateWhen { false }
        showStandardStreams = true
    }
}

tasks {
    register("assembleUp") {
        dependsOn("assemble", "composeUp")
    }
    getByName("composeUp").shouldRunAfter("assemble")
}

dockerCompose {
    dockerComposeWorkingDirectory.set(File("./e2e"))
    removeVolumes.set(true)
    waitForTcpPorts.set(false)
}
