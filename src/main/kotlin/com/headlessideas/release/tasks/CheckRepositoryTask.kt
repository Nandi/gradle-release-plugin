package com.headlessideas.release.tasks

import org.gradle.api.tasks.TaskAction

abstract class CheckRepositoryTask : DefaultReleaseTask() {
    companion object {
        const val NAME = "checkRepository"
    }

    init {
        description = "Checks if the repository is ready for release"
    }

    @TaskAction
    fun checkRepository() {
        with(extension.gitService) {
            val ref = current

            checkoutDevelopBranch()
            verifyCurrent()

            project.version

            checkoutMainBranch()
            verifyCurrent()

            checkoutBranch(ref)
        }
    }
}
