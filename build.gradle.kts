import java.util.*

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("app.cash.licensee:licensee-gradle-plugin:1.7.0")
    }
}

apply(plugin = "app.cash.licensee")

plugins {
    id("com.diffplug.spotless")
    id("org.mikeneck.graalvm-native-image")
    id("com.github.johnrengelman.shadow")
    id("com.apollographql.apollo3")
    id("com.avast.gradle.docker-compose")
    id("io.gitlab.arturbosch.detekt")
    kotlin("jvm")
}

val cliGroup: String by project
val projectVersion: String by project
val apolloVersion: String by project
val commonsLang3Version: String by project
val cliktVersion: String by project
val bouncycastleVersion: String by project
val jupiterVersion: String by project
val commonsIoVersion: String by project
val auth0JwtVersion: String by project
val vertxVersion: String by project
val slf4jVersion: String by project

group = cliGroup
version = project.properties["cliVersion"] as String? ?: projectVersion

repositories {
    mavenCentral()
    maven(url = "https://pkg.sourceplus.plus/sourceplusplus/protocol")
}

val graphqlLibs = configurations.create("graphqlLibs")

dependencies {
    implementation("com.apollographql.apollo3:apollo-runtime:$apolloVersion")
    api("com.apollographql.apollo3:apollo-api:$apolloVersion")

    implementation("plus.sourceplus:protocol:$projectVersion")
    graphqlLibs("plus.sourceplus:protocol:$projectVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-nop:$slf4jVersion")
    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-tcp-eventbus-bridge:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
    implementation("org.bouncycastle:bcprov-jdk15on:$bouncycastleVersion")
    implementation("org.bouncycastle:bcpkix-jdk15on:$bouncycastleVersion")
    implementation("commons-io:commons-io:$commonsIoVersion")
    implementation("com.auth0:java-jwt:$auth0JwtVersion")
    implementation("eu.geekplace.javapinning:java-pinning-core:1.2.0")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("org.slf4j:slf4j-simple:$slf4jVersion")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
}

//remove slf4j-nop from test classpath (use slf4j-simple instead)
tasks.withType<Test> {
    useJUnitPlatform()
    classpath = classpath.filter { !it.toString().contains("slf4j-nop") }
}

configure<app.cash.licensee.LicenseeExtension> {
    ignoreDependencies("plus.sourceplus", "protocol")
    allow("Apache-2.0")
    allow("MIT")
    allowUrl("https://raw.githubusercontent.com/apollographql/apollo-kotlin/main/LICENSE") //MIT
    allowUrl("https://raw.githubusercontent.com/auth0/java-jwt/master/LICENSE") //MIT
    allowUrl("https://www.bouncycastle.org/licence.html") //MIT
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

configurations {
    create("empty")
}

nativeImage {
    dependsOn("shadowJar")
    setClasspath(File(project.buildDir, "libs/spp-cli-${project.version}.jar"))
    runtimeClasspath = configurations.getByName("empty")
    if (System.getenv("GRAALVM_HOME") != null) {
        graalVmHome = System.getenv("GRAALVM_HOME")
    }
    buildType { build ->
        build.executable(main = "spp.cli.Main")
    }
    executableName = "spp-cli"
    outputDirectory = file("$buildDir/graal")
    arguments("--no-fallback")
    arguments("-H:+ReportExceptionStackTraces")
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
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()

    val isIntegrationProfile = System.getProperty("test.profile") == "integration"
    val runningSpecificTests = gradle.startParameter.taskRequests.isNotEmpty()

    //exclude integration tests unless requested
    if (!isIntegrationProfile && !runningSpecificTests) {
        exclude("integration/**", "**/*IntegrationTest.class", "**/*ITTest.class")
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
    getByName("composeUp").mustRunAfter("assemble")
}

dockerCompose {
    waitForTcpPorts.set(false)
}

apollo {
    service("service") {
        packageNamesFromFilePaths("spp.cli.protocol")
    }
}

//todo: find specific task to add dependsOn() to
//ensure graphqlLibs() dependency is available
tasks.all {
    if (findProject(":protocol") != null) {
        dependsOn(":protocol:jar")
    }
}

tasks.create<Copy>("importProtocolFiles") {
    configurations.getByName("graphqlLibs").asFileTree.forEach {
        if (it.name.startsWith("protocol-")) {
            from(zipTree(it)) {
                exclude("META-INF/**")
                exclude("spp/**")
            }
        }
    }
    into(file("src/main"))
}
tasks.getByName("checkApolloVersions").dependsOn("importProtocolFiles")
tasks.getByName("test").dependsOn("importProtocolFiles")
tasks.getByName("generateServiceApolloSchema").dependsOn("importProtocolFiles")
tasks.getByName("generateServiceApolloUsedCoordinates").dependsOn("importProtocolFiles")
tasks.getByName("processResources").dependsOn("importProtocolFiles")
tasks.getByName("detekt").dependsOn("importProtocolFiles")

spotless {
    kotlin {
        targetExclude("**/generated/**")

        val startYear = 2022
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val copyrightYears = if (startYear == currentYear) {
            "$startYear"
        } else {
            "$startYear-$currentYear"
        }

        val jetbrainsProject = findProject(":protocol") ?: rootProject
        val licenseHeader = Regex("( . Copyright [\\S\\s]+)")
            .find(File(jetbrainsProject.projectDir, "LICENSE").readText())!!
            .value.lines().joinToString("\n") {
                if (it.trim().isEmpty()) {
                    " *"
                } else {
                    " * " + it.trim()
                }
            }
        val formattedLicenseHeader = buildString {
            append("/*\n")
            append(
                licenseHeader.replace(
                    "Copyright [yyyy] [name of copyright owner]",
                    "Source++, the continuous feedback platform for developers.\n" +
                            " * Copyright (C) $copyrightYears CodeBrig, Inc."
                ).replace(
                    "http://www.apache.org/licenses/LICENSE-2.0",
                    "    http://www.apache.org/licenses/LICENSE-2.0"
                )
            )
            append("/")
        }
        licenseHeader(formattedLicenseHeader)
    }
}

detekt {
    parallel = true
    buildUponDefaultConfig = true
    config.setFrom(arrayOf(File(project.rootDir, "detekt.yml")))
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}
