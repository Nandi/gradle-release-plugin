package com.headlessideas.release.util

import com.headlessideas.release.ReleasePlugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.nio.file.Paths
import kotlin.reflect.KClass

inline fun <reified T : Task> Project.register(name: String, noinline taskConfiguration: T.() -> Unit = {}): TaskProvider<T> {
    return project.tasks.register(name, T::class.java, taskConfiguration)
}

inline fun <reified T : Any> Project.extension(klass: KClass<T>): T {
    return project.extensions.findByType(klass.java)
        ?: throw GradleException("${project.displayName.capitalize()} must have plugin applied: ${ReleasePlugin.ID}")
}

fun Project.isGitRepository(): Boolean {
    val gitDir = Paths.get(rootProject.rootDir.absolutePath, ".git").toFile()
    return gitDir.exists() && gitDir.isDirectory
}
