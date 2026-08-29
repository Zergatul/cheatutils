package com.zergatul.cheatutils

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.File
import java.util.Properties
import java.util.concurrent.TimeUnit

open class CheatUtilsExtension(private val project: Project) {

    private val commonProperties: Properties by lazy {
        val file = project.projectDir.resolve("../common/gradle.properties")
        val properties = Properties()
        if (file.isFile) {
            file.inputStream().use { properties.load(it) }
        } else {
            error("Missing common gradle.properties at: ${file.absolutePath}")
        }
        properties
    }

    private val isGitAvailable: Boolean by lazy {
        try {
            val process = ProcessBuilder("git", "--version")
                .redirectErrorStream(true)
                .start()
            process.waitFor(1, TimeUnit.SECONDS) && process.exitValue() == 0
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
            ?: error("Missing mod_version in common/gradle.properties")
}

class CheatUtilsGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("cheatutils", CheatUtilsExtension::class.java, project)
        val generatedResourcesDir = project.layout.buildDirectory.dir("generated/resources")

        project.plugins.withType(JavaPlugin::class.java) {
            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            sourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME) {
                resources.srcDir(generatedResourcesDir)
            }
        }

        project.tasks.register("generateCommitsJson") {
            val cheatUtilsCommit = extension.getCommitByPath("..")
            val scriptingLanguageCommit = extension.getCommitByPath("../java-scripting-language")

            inputs.property("cheatutilsCommit", cheatUtilsCommit)
            inputs.property("scriptingLanguageCommit", scriptingLanguageCommit)

            val outputFile = generatedResourcesDir.map { it.file("commits.json") }
            outputs.file(outputFile)

            doLast {
                val file = outputFile.get().asFile
                file.parentFile.mkdirs()
                file.writeText(
                    """{"cheatutils":"${cheatUtilsCommit.get()}","java-scripting-language":"${scriptingLanguageCommit.get()}"}"""
                )
            }
        }

        project.tasks.withType(ProcessResources::class.java).configureEach {
            dependsOn("generateCommitsJson")
        }
    }
}