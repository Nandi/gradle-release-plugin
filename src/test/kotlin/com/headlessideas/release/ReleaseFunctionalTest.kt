package com.headlessideas.release

import com.headlessideas.util.addAndCommit
import com.headlessideas.util.initGitRepository
import com.headlessideas.util.tags
import org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Release Plugin functional test")
class ReleaseFunctionalTest {
    private val testProjectDir = TemporaryFolder()
    private lateinit var buildFile: File
    private lateinit var git: Git

    @BeforeEach
    fun setUp() {
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
        println(testProjectDir.root)
    }

    @AfterEach
    fun tearDown() {
        testProjectDir.delete()
    }

    @Test
    fun `Release successful with clean repository`() {
        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("release", "-Prelease.stage=final", "-Prelease.scope=major")
            .withPluginClasspath()
            .build()

        assertEquals(TaskOutcome.SUCCESS, runner.task(":release")?.outcome)
        assertTrue(git.tags().any { it == "1.0.0" })
        assertEquals(git.repository.branch, "master")
    }

    @Test
    fun `Release fails if stage not set`() {
        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("release", "-Prelease.scope=major")
            .withPluginClasspath()
            .buildAndFail()

        assertEquals(TaskOutcome.FAILED, runner.task(":release")?.outcome)
    }

    @Test
    fun `Release fails if repository is dirty`() {
        testProjectDir.newFile("dirty").writeText("dirty")

        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("release", "-Prelease.stage=final", "-Prelease.scope=major")
            .withPluginClasspath()
            .buildAndFail()

        assertEquals(TaskOutcome.FAILED, runner.task(":checkRepository")?.outcome)
    }
}
