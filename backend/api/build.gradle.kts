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
// Configuration-cache safe: only serializable values enter the task action.
// ---------------------------------------------------------------------------
val generatedVersionDir = layout.buildDirectory.dir("generated/version")
// Capture at configuration time — Project refs are illegal inside doLast with CC
val repoRootPath: String = rootProject.layout.projectDirectory.asFile.absolutePath

val generateVersionJson by tasks.registering {
    val outputDir = generatedVersionDir
    val gitWorkingDir = repoRootPath

    outputs.dir(outputDir)
    // Re-run when HEAD moves (best-effort; missing in non-git checkouts)
    val gitHead = file("$gitWorkingDir/.git/HEAD")
    if (gitHead.exists()) {
        inputs.file(gitHead)
    }

    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()

        fun runGit(vararg args: String): Pair<Int, String> {
            val pb = ProcessBuilder("git", *args)
                .redirectErrorStream(true)
                .directory(java.io.File(gitWorkingDir))
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
