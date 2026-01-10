package com.zergatul.cheatutils

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.plugins.JavaPlugin
import java.io.File
import java.util.concurrent.TimeUnit
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.language.jvm.tasks.ProcessResources
import java.util.*

open class CheatUtilsExtension(private val project: Project) {

    private val commonProperties: Properties by lazy {
        val file = project.projectDir.resolve("../common/gradle.properties")
        val props = Properties()
        if (file.isFile) {
            file.inputStream().use { props.load(it) }
        } else {
            error("Missing common gradle.properties at: ${file.absolutePath}")
        }
        props
    }

    private val isGitAvailable: Boolean by lazy {
        try {
            val process = ProcessBuilder("git", "--version")
                .redirectErrorStream(true)
                .start()
            process.waitFor(1, TimeUnit.SECONDS)
            process.exitValue() == 0
        } catch (_: Exception) {
            project.logger.warn("Git not found or failed to run")
            false
        }
    }

    fun getCommitByPath(path: String): Provider<String> =
        if (isGitAvailable) {
            project.providers.exec {
                workingDir(File(path))
                commandLine("git", "rev-parse", "HEAD")
            }.standardOutput.asText.map { it.trim() }
        } else {
            project.providers.provider { "" }
        }

    fun getModVersion(): String =
        commonProperties.getProperty("mod_version")
}

class CheatUtilsGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("cheatutils", CheatUtilsExtension::class.java, project)
        val generatedResourcesDir = project.layout.buildDirectory.dir("generated/resources")

        // add generated resources directory to source sets
        project.plugins.withType(JavaPlugin::class.java) {
            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            sourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME) {
                resources.srcDir(generatedResourcesDir)
            }
        }

        project.tasks.register("generateCommitsJson") {
            val cuCommitProvider = ext.getCommitByPath("..")
            val jslCommitProvider = ext.getCommitByPath("../java-scripting-language")

            inputs.property("cuCommit", cuCommitProvider)
            inputs.property("jslCommit", jslCommitProvider)

            val outputFile = generatedResourcesDir.map { it.file("commits.json") }
            outputs.file(outputFile)

            doLast {
                val file = outputFile.get().asFile
                file.parentFile.mkdirs()
                val cuCommit = cuCommitProvider.get()
                val jslCommit = jslCommitProvider.get()
                file.writeText("""{"cheatutils":"$cuCommit","java-scripting-language":"$jslCommit"}""")
            }
        }

        project.tasks.withType(ProcessResources::class.java).configureEach {
            dependsOn("generateCommitsJson")
        }
    }
}