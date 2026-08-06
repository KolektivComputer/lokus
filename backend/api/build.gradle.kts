plugins {
    id("capital.yuri.locus.kotlin-application")
    id("capital.yuri.locus.config-resources")
    alias(libs.plugins.koin.compiler)
}

dependencies {
    implementation(project(":backend:common"))
    implementation(project(":backend:core"))
    // Extensions are discovered at runtime from extensions/*.jar — not on the
    // compile classpath. Build ships jars via Docker / example.d.

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
// version.json — classpath /version.json (tag, commit, dirty, updateUrl)
// Git is optional: Docker/alpine builds often have no git binary. Override via:
//   -Plocus.commit=… -Plocus.tag=… -Plocus.updateUrl=…
// or env LOCUS_COMMIT / LOCUS_TAG / LOCUS_UPDATE_URL
// ---------------------------------------------------------------------------
val generatedVersionDir = layout.buildDirectory.dir("generated/version")
val repoRootPath: String = rootProject.layout.projectDirectory.asFile.absolutePath

fun propOrEnv(prop: String, env: String): String? =
    (findProperty(prop) as String?)?.takeIf { it.isNotBlank() }
        ?: System.getenv(env)?.takeIf { it.isNotBlank() }

val overrideCommit = propOrEnv("locus.commit", "LOCUS_COMMIT")
val overrideTag = propOrEnv("locus.tag", "LOCUS_TAG")
val overrideUpdateUrl = propOrEnv("locus.updateUrl", "LOCUS_UPDATE_URL")

val generateVersionJson by tasks.registering {
    val outputDir = generatedVersionDir
    val gitWorkingDir = repoRootPath
    val forcedCommit = overrideCommit
    val forcedTag = overrideTag
    val forcedUpdateUrl = overrideUpdateUrl

    outputs.dir(outputDir)
    val gitHead = file("$gitWorkingDir/.git/HEAD")
    if (gitHead.exists()) {
        inputs.file(gitHead)
    }

    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()

        fun runGit(vararg args: String): Pair<Int, String> = try {
            val pb = ProcessBuilder("git", *args)
                .redirectErrorStream(true)
                .directory(File(gitWorkingDir))
            val proc = pb.start()
            val text = proc.inputStream.bufferedReader().readText().trim()
            proc.waitFor() to text
        } catch (_: Exception) {
            // No git binary (typical in minimal Docker build images)
            -1 to ""
        }

        val (_, gitCommit) = runGit("rev-parse", "HEAD")
        val (tagCode, tagOut) = runGit("describe", "--tags", "--exact-match")
        val (dirtyCode, dirtyOut) = runGit("status", "--porcelain")

        val commit = forcedCommit ?: gitCommit.takeIf { it.isNotBlank() }
        val tag = forcedTag ?: tagOut.takeIf { tagCode == 0 && it.isNotBlank() }
        val dirty = if (forcedCommit != null || forcedTag != null) {
            false
        } else {
            dirtyCode == 0 && dirtyOut.isNotBlank()
        }
        val updateUrl = if (dirty) null else forcedUpdateUrl

        fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")

        val json = buildString {
            appendLine("{")
            append("  \"tag\": ")
            if (tag != null) append('"').append(esc(tag)).append('"') else append("null")
            appendLine(",")
            append("  \"commit\": ")
            if (commit != null) append('"').append(esc(commit)).append('"') else append("null")
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
