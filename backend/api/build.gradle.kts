plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    application

    alias(libs.plugins.koin.compiler)
}

group = "capital.yuri"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.logging)
    implementation(libs.bundles.database)

    implementation(libs.argon2)
    implementation(libs.quartz)
    implementation(libs.clikt)
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("capital.yuri.locus.platform.MainKt")
    applicationName = "locus"
}

koinCompiler {
    compileSafety = false
    strictSafety = false
}

// ---------------------------------------------------------------------------
// version.json — embedded as /version.json on the classpath
// ---------------------------------------------------------------------------
val generateVersionJson by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/version")
    outputs.dir(outputDir)

    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()

        fun git(vararg args: String): String? = try {
            providers.exec {
                commandLine("git", *args)
                isIgnoreExitValue = true
            }.standardOutput.asText.get().trim().ifBlank { null }
                .takeIf {
                    providers.exec {
                        commandLine("git", *args)
                        isIgnoreExitValue = true
                    }.result.get().exitValue == 0
                }
        } catch (_: Exception) {
            null
        }

        // Simpler portable capture
        fun runGit(vararg args: String): Pair<Int, String> {
            val pb = ProcessBuilder("git", *args)
                .redirectErrorStream(true)
                .directory(rootProject.projectDir)
            val proc = pb.start()
            val text = proc.inputStream.bufferedReader().readText().trim()
            val code = proc.waitFor()
            return code to text
        }

        val (_, commit) = runGit("rev-parse", "HEAD")
        val (tagCode, tagOut) = runGit("describe", "--tags", "--exact-match")
        val tag = if (tagCode == 0) tagOut else null
        val (dirtyCode, dirtyOut) = runGit("status", "--porcelain")
        val dirty = dirtyCode == 0 && dirtyOut.isNotBlank()

        val json = buildString {
            appendLine("{")
            append("  \"tag\": ")
            if (tag != null) append("\"").append(tag).append("\"") else append("null")
            appendLine(",")
            append("  \"commit\": ")
            if (commit.isNotBlank()) append("\"").append(commit).append("\"") else append("null")
            appendLine(",")
            append("  \"dirty\": ").append(dirty).appendLine()
            appendLine("}")
        }
        dir.resolve("version.json").writeText(json)
    }
}

sourceSets {
    main {
        resources {
            srcDir(generateVersionJson.map { it.outputs.files.asPath })
        }
    }
}

tasks.named("processResources") {
    dependsOn(generateVersionJson)
}

tasks.test {
    useJUnitPlatform()
}
