package com.headlessideas.release.tasks

import com.headlessideas.release.extension.ReleaseExtension
import com.headlessideas.release.util.extension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal

open class DefaultReleaseTask : DefaultTask() {
    @get:Internal
    protected val extension: ReleaseExtension = project.extension(ReleaseExtension::class)

    init {
        group = "release"
    }
}
