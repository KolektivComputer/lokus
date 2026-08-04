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
// version.json — classpath resource /version.json (tag + commit + dirty)
// ---------------------------------------------------------------------------
val generatedVersionDir = layout.buildDirectory.dir("generated/version")

val generateVersionJson by tasks.registering {
    outputs.dir(generatedVersionDir)

    doLast {
        val dir = generatedVersionDir.get().asFile
        dir.mkdirs()

        fun runGit(vararg args: String): Pair<Int, String> {
            val pb = ProcessBuilder("git", *args)
                .redirectErrorStream(true)
                .directory(rootProject.projectDir)
            val proc = pb.start()
            val text = proc.inputStream.bufferedReader().readText().trim()
            return proc.waitFor() to text
        }

        val (_, commit) = runGit("rev-parse", "HEAD")
        val (tagCode, tagOut) = runGit("describe", "--tags", "--exact-match")
        val tag = tagOut.takeIf { tagCode == 0 && it.isNotBlank() }
        val (dirtyCode, dirtyOut) = runGit("status", "--porcelain")
        val dirty = dirtyCode == 0 && dirtyOut.isNotBlank()

        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")

        val json = buildString {
            appendLine("{")
            append("  \"tag\": ")
            if (tag != null) append('"').append(esc(tag)).append('"') else append("null")
            appendLine(",")
            append("  \"commit\": ")
            if (commit.isNotBlank()) append('"').append(esc(commit)).append('"') else append("null")
            appendLine(",")
            append("  \"dirty\": ").append(dirty).appendLine()
            appendLine("}")
        }
        dir.resolve("version.json").writeText(json)
    }
}

sourceSets.named("main") {
    resources.srcDir(generatedVersionDir)
}

tasks.named("processResources") {
    dependsOn(generateVersionJson)
}

tasks.test {
    useJUnitPlatform()
}
