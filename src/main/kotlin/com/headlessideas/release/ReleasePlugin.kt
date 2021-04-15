package com.headlessideas.release

import com.headlessideas.release.extension.ReleaseExtension
import com.headlessideas.release.extension.ReleaseExtension.Companion.NAME
import com.headlessideas.release.extension.releaseExtension
import com.headlessideas.release.tasks.CheckRepositoryTask
import com.headlessideas.release.tasks.PrintVersionTask
import com.headlessideas.release.tasks.ReleaseNextVersionTask
import com.headlessideas.release.util.DelayedVersion
import com.headlessideas.release.util.isGitRepository
import com.headlessideas.release.util.register
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
abstract class ReleasePlugin : Plugin<Project> {
    companion object {
        const val ID = "com.headlessideas.release.plugin"
    }

    override fun apply(project: Project) {
        project.run { configureProject() }
    }

    private fun Project.configureProject() {
        setupExtension()

        if (!isGitRepository()) {
            logger.error("$ID is dependent on a git repository in the root project directory [ ${project.rootProject.projectDir} ]")
            return
        }

        setupProjectVersion()
        setupTasks()
    }

    private fun Project.setupProjectVersion() {
        val sharedVersion = DelayedVersion { releaseExtension.version() }
        allprojects {
            it.version = sharedVersion
        }
    }

    private fun Project.setupExtension() {
        project.extensions.findByType(
            ReleaseExtension::class.java
        ) ?: project.extensions.create(NAME, ReleaseExtension::class.java, project)
    }

    private fun Project.setupTasks() {
        val checker = register<CheckRepositoryTask>(CheckRepositoryTask.NAME)

        register<ReleaseNextVersionTask>(ReleaseNextVersionTask.NAME) {
            dependsOn(checker)
        }

        register<PrintVersionTask>(PrintVersionTask.NAME)
    }
}
