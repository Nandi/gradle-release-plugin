package com.headlessideas.release

import com.headlessideas.util.addAndCommit
import com.headlessideas.util.initGitRepository
import org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Release Plugin functional test")
class PrintVersionFunctionalTest {
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
    fun `final patch version`() {
        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("printVersion", "-q", "-Prelease.stage=final", "-Prelease.scope=patch")
            .withPluginClasspath()
            .build()

        print(runner.output)
        assertEquals(TaskOutcome.SUCCESS, runner.task(":printVersion")?.outcome)
        assertEquals("0.0.1", runner.output.trim())
    }

    @Test
    fun `final minor version`() {
        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("printVersion", "-q", "-Prelease.stage=final", "-Prelease.scope=minor")
            .withPluginClasspath()
            .build()

        print(runner.output)
        assertEquals(TaskOutcome.SUCCESS, runner.task(":printVersion")?.outcome)
        assertEquals("0.1.0", runner.output.trim())
    }

    @Test
    fun `final major version`() {
        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("printVersion", "-q", "-Prelease.stage=final", "-Prelease.scope=major")
            .withPluginClasspath()
            .build()

        print(runner.output)
        assertEquals(TaskOutcome.SUCCESS, runner.task(":printVersion")?.outcome)
        assertEquals("1.0.0", runner.output.trim())
    }

    @Test
    fun `final default version`() {
        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("printVersion", "-q", "-Prelease.stage=final")
            .withPluginClasspath()
            .build()

        print(runner.output)
        assertEquals(TaskOutcome.SUCCESS, runner.task(":printVersion")?.outcome)
        assertEquals("0.1.0", runner.output.trim())
    }

    @Test
    fun `final version in clean repository with stage`() {
        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("printVersion", "-q", "-Prelease.stage=final")
            .withPluginClasspath()
            .build()

        print(runner.output)
        assertEquals(TaskOutcome.SUCCESS, runner.task(":printVersion")?.outcome)
        assertEquals("0.1.0", runner.output.trim())
    }

    @Test
    fun `insignificant version in dirty repository without stage`() {
        testProjectDir.newFile("dirty").writeText("dirty")

        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("printVersion", "-q")
            .withPluginClasspath()
            .build()

        print(runner.output)
        assertEquals(TaskOutcome.SUCCESS, runner.task(":printVersion")?.outcome)
        assertNotEquals("0.1.0", runner.output.trim())
        assertTrue(runner.output.startsWith("0.1.0"))
    }
}
