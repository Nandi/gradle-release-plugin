package com.headlessideas.release.extension

import com.headlessideas.release.util.GitService
import com.headlessideas.release.util.VersionService
import com.headlessideas.release.util.extension
import org.ajoberstar.reckon.core.Version
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class ReleaseExtension @Inject constructor(
    private val project: Project,
    objectFactory: ObjectFactory,
    providerFactory: ProviderFactory,
) {
    companion object {
        const val NAME = "releaseConfig"
        const val SCOPE_PROP = "release.scope"
        const val STAGE_PROP = "release.stage"
    }

    private val mainBranchProperty = objectFactory.property(String::class.java).convention("master")
    private val developBranchProperty = objectFactory.property(String::class.java).convention("develop")
    private val releaseBranchPrefixProperty = objectFactory.property(String::class.java).convention("release")
    private val releaseStagesProperty = objectFactory.listProperty(String::class.java).convention(listOf("rc", "final"))
    private val remoteProperty = objectFactory.property(String::class.java).convention("origin")
    private val tagPrefixProperty = objectFactory.property(String::class.java).convention("")

    private val scope: String = providerFactory.gradleProperty(SCOPE_PROP).getOrElse("minor")
    val stage: String? = providerFactory.gradleProperty(STAGE_PROP).orNull

    private val versionService: VersionService by lazy { VersionService(gitService.repository, releaseStagesProperty.get(), stage, scope) }

    val gitService: GitService by lazy { GitService(project.rootProject.layout.projectDirectory.asFile, mainBranch, developBranch, releaseBranchPrefix) }

    /**
     * This is provider for the mainBranch property.
     */
    val mainBranchProvider: Provider<String>
        get() = mainBranchProperty

    /**
     * This is mainBranch property.
     */
    var mainBranch: String
        get() = mainBranchProperty.get()
        set(value) = mainBranchProperty.set(value)

    /**
     * This is provider for the developBranch property.
     */
    val developBranchProvider: Provider<String>
        get() = developBranchProperty

    /**
     * This is developBranch property.
     */
    var developBranch: String
        get() = developBranchProperty.get()
        set(value) = developBranchProperty.set(value)

    /**
     * This is provider for the releaseBranchPrefix property.
     */
    val releaseBranchPrefixProvider: Provider<String>
        get() = releaseBranchPrefixProperty

    /**
     * This is releaseBranchPrefix property.
     */
    var releaseBranchPrefix: String
        get() = releaseBranchPrefixProperty.get()
        set(value) = releaseBranchPrefixProperty.set(value)

    /**
     * This is provider for the releaseStages property.
     */
    val releaseStagesProvider: Provider<List<String>>
        get() = releaseStagesProperty

    /**
     * This is releaseBranchPrefix property.
     */
    var releaseStages: MutableList<String>
        get() = releaseStagesProperty.get()
        set(value) = releaseStagesProperty.set(value)

    /**
     * This is provider for the remote property.
     */
    val remoteProvider: Provider<String>
        get() = remoteProperty

    /**
     * This is releaseBranchPrefix property.
     */
    var remote: String
        get() = remoteProperty.get()
        set(value) = remoteProperty.set(value)

    /**
     * This is provider for the mainBranch property.
     */
    val tagPrefixProvider: Provider<String>
        get() = tagPrefixProperty

    /**
     * This is mainBranch property.
     */
    var tagPrefix: String
        get() = tagPrefixProperty.get()
        set(value) = tagPrefixProperty.set(value)

    fun version(): Version = versionService.version()
}

val Project.releaseExtension get() = extension(ReleaseExtension::class)
