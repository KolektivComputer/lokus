plugins {
    id("capital.yuri.locus.kotlin-application")
    id("capital.yuri.locus.config-resources")
    alias(libs.plugins.koin.compiler)
}

dependencies {
    implementation(project(":backend:common"))
    implementation(project(":backend:core"))

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

locusApplication {
    mainClass.set("capital.yuri.locus.platform.MainKt")
    applicationName.set("locus")
}

locusConfigResources {
    enabled.set(true)
}

koinCompiler {
    compileSafety = false
    strictSafety = false
}

// ---------------------------------------------------------------------------
// version.json — /version.json (tag, commit, dirty, updateUrl)
// ---------------------------------------------------------------------------
val generatedVersionDir = layout.buildDirectory.dir("generated/version")
val repoRootPath: String = rootProject.layout.projectDirectory.asFile.absolutePath
val releaseUpdateUrl: String? =
    (findProperty("locus.updateUrl") as String?)?.takeIf { it.isNotBlank() }

val generateVersionJson by tasks.registering {
    val outputDir = generatedVersionDir
    val gitWorkingDir = repoRootPath
    val configuredUpdateUrl = releaseUpdateUrl

    outputs.dir(outputDir)
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
        val updateUrl = if (dirty) null else configuredUpdateUrl

        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")

        val json = buildString {
            appendLine("{")
            append("  \"tag\": ")
            if (tag != null) append('"').append(esc(tag)).append('"') else append("null")
            appendLine(",")
            append("  \"commit\": ")
            if (commit.isNotBlank()) append('"').append(esc(commit)).append('"') else append("null")
            appendLine(",")
            append("  \"dirty\": ").append(dirty).appendLine(",")
            append("  \"updateUrl\": ")
            if (updateUrl != null) append('"').append(esc(updateUrl)).append('"') else append("null")
            appendLine()
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
