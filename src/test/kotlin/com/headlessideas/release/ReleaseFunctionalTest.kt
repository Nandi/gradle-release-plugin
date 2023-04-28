package com.headlessideas.release

import com.headlessideas.util.addAndCommit
import com.headlessideas.util.initGitRepository
import com.headlessideas.util.tags
import io.kotest.core.spec.style.FreeSpec
import org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions
import java.io.File

class ReleaseFunctionalTest : FreeSpec({
    val testProjectDir = TemporaryFolder()
    lateinit var buildFile: File
    lateinit var git: Git

    beforeTest {
        testProjectDir.create()
        git = initGitRepository(testProjectDir.root)
        testProjectDir.newFile(".gitignore").writeText(
            """
            .gitignore
            .gradle/
            """.trimIndent()
        )
        buildFile = testProjectDir.newFile("build.gradle.kts")
        buildFile.writeText(
            """
            plugins {
                id("com.headlessideas.release.plugin")
            }
            """.trimIndent()
        )
        git.addAndCommit(buildFile.name)
    }

    afterTest {
        testProjectDir.delete()
    }

    "Given clean repository then create successful release" {
        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("release", "-Prelease.stage=final", "-Prelease.scope=major")
            .withPluginClasspath()
            .build()

        Assertions.assertEquals(TaskOutcome.SUCCESS, runner.task(":release")?.outcome)
        Assertions.assertTrue(git.tags().any { it == "1.0.0" })
        Assertions.assertEquals(git.repository.branch, "master")
    }

    "Given clean repository when stage is not set then fail release" {
        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("release", "-Prelease.scope=major")
            .withPluginClasspath()
            .buildAndFail()

        Assertions.assertEquals(TaskOutcome.FAILED, runner.task(":release")?.outcome)
    }

    "Given dirty repository then fail release" {
        testProjectDir.newFile("dirty").writeText("dirty")

        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("release", "-Prelease.stage=final", "-Prelease.scope=major")
            .withPluginClasspath()
            .buildAndFail()

        Assertions.assertEquals(TaskOutcome.FAILED, runner.task(":checkRepository")?.outcome)
    }
})
