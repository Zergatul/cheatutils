package com.zergatul.cheatutils

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Updates developer-maintained source files in place")
abstract class GenerateImmutableConfigsTask : DefaultTask() {

    @get:InputDirectory
    abstract val sourceDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        sourceDirectory.get().asFile.walkTopDown()
            .filter { it.isFile && it.extension == "java" }
            .sortedBy { it.path }
            .forEach { file ->
                val source = file.readText()
                val generated = try {
                    ImmutableConfigGenerator.generate(source)
                } catch (exception: IllegalArgumentException) {
                    throw IllegalArgumentException("${file.name}: ${exception.message}", exception)
                }
                if (source != generated) {
                    file.writeText(generated)
                    logger.lifecycle("Generated ${file.name}")
                }
            }
    }
}