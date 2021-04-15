package com.headlessideas.release.tasks

import com.headlessideas.release.extension.ReleaseExtension
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

abstract class ReleaseNextVersionTask : DefaultReleaseTask() {
    companion object {
        const val NAME = "release"
    }

    init {
        description = "Runs the full release procedure on git repository"
    }

    @TaskAction
    fun release() {
        val version = project.version.toString()

        if (extension.stage != "final") throw GradleException("Can only create release branch on final versions. Please add -P${ReleaseExtension.STAGE_PROP}=final to the gradle command.")

        // Create Release branch
        with(extension.gitService) {
            checkoutDevelopBranch()
            checkoutReleaseBranch(version)
            mergeFromMain()
        }

        // Merge release branch into main branch
        with(extension.gitService) {
            checkoutReleaseBranch(version)
            mergeIntoMainBranch()
        }

        // Create version tag
        extension.gitService.tag(version, version)

        // Push main branch and tags to remote
        with(extension.gitService) {
            checkoutMainBranch()
            verifyCurrent()
            pushChanges()
            pushTags()
        }

        // Clean up
        with(extension.gitService) {
            deleteReleaseBranch(version)
        }
    }
}
