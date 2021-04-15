package com.headlessideas.release.tasks

import org.gradle.api.tasks.TaskAction

abstract class PrintVersionTask : DefaultReleaseTask() {
    companion object {
        const val NAME = "printVersion"
    }

    init {
        description = "Prints the inferred version"
    }

    @TaskAction
    fun printVersion() {
        println("${project.version}")
    }
}
