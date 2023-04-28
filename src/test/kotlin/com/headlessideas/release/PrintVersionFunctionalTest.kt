package com.headlessideas.release

import com.headlessideas.util.addAndCommit
import com.headlessideas.util.initGitRepository
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions
import java.io.File

class PrintVersionFunctionalTest : FreeSpec({
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

    "Given final version" - {
        "when no scope given then default to minor" {
            val runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("printVersion", "-q", "-Prelease.stage=final")
                .withPluginClasspath()
                .build()

            Assertions.assertEquals(TaskOutcome.SUCCESS, runner.task(":printVersion")?.outcome)
            Assertions.assertEquals("0.1.0", runner.output.trim())
        }

        withData(
            nameFn = { "when scope is ${it.first} then version should be ${it.second}" },
            "patch" to "0.0.1",
            "minor" to "0.1.0",
            "major" to "1.0.0",
        ) { (scope, version) ->
            val runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("printVersion", "-q", "-Prelease.stage=final", "-Prelease.scope=$scope")
                .withPluginClasspath()
                .build()

            Assertions.assertEquals(TaskOutcome.SUCCESS, runner.task(":printVersion")?.outcome)
            Assertions.assertEquals(version, runner.output.trim())
        }
    }

    "Given final version when repository is dirty then return insignificant version" {
        testProjectDir.newFile("dirty").writeText("dirty")

        val runner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments("printVersion", "-q")
            .withPluginClasspath()
            .build()

        Assertions.assertEquals(TaskOutcome.SUCCESS, runner.task(":printVersion")?.outcome)
        Assertions.assertNotEquals("0.1.0", runner.output.trim())
        Assertions.assertTrue(runner.output.startsWith("0.1.0"))
    }
})