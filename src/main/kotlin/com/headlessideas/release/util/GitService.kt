package com.headlessideas.release.util

import com.headlessideas.release.ReleasePlugin
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeCommand
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.Repository.shortenRefName
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.RemoteConfig
import org.gradle.api.GradleException
import java.io.File
import java.nio.file.Paths

@Suppress("unused")
class GitService(
    directory: File,
    private val mainBranch: String = "main",
    private val developBranch: String = "develop",
    private val releaseBranchPrefix: String = "release",
    private val remote: String = "origin",
) {
    private val git: Git
    private val hasRemote: Boolean
    private val clean: Boolean
        get() = git.status().call().isClean

    val current: Ref
        get() = repository.exactRef(Constants.HEAD)

    init {
        val repository = FileRepositoryBuilder()
            .setGitDir(Paths.get(directory.absolutePath, ".git").toFile())
            .readEnvironment()
            .findGitDir()
            .build()
        git = Git.wrap(repository)
        hasRemote = getRemoteList().any { it.name == remote }
    }

    val repository: Repository = git.repository

    fun checkoutMainBranch() {
        val branch = getOrCreateBranch(mainBranch)
        checkoutBranch(branch)
    }

    fun mergeFromMain() {
        val branch = getBranch(mainBranch)
            ?: throw GradleException("${ReleasePlugin.ID} expects branch $mainBranch to exist before it can continue")

        merge(branch, current)
    }

    fun mergeIntoMainBranch() {
        val branch = getOrCreateBranch(mainBranch)
        merge(current, branch)
    }

    fun checkoutDevelopBranch() {
        val branch = getBranch(developBranch)
            ?: throw GradleException("${ReleasePlugin.ID} expects branch $developBranch to exist before it can continue")

        checkoutBranch(branch)
    }

    fun checkoutReleaseBranch(version: String) {
        val branch = getOrCreateBranch("$releaseBranchPrefix/$version")
        checkoutBranch(branch)
    }

    fun deleteReleaseBranch(version: String) {
        val branch = getBranch("$releaseBranchPrefix/$version")
            ?: throw GradleException("${ReleasePlugin.ID} expects branch $developBranch to exist before it can continue")

        deleteBranch(branch)
    }

    fun checkoutBranch(branch: Ref) {
        val checkout = git.checkout()
        checkout.setName(shortenRefName(branch.name))
        if (branch.name.startsWith(remote)) {
            checkout.setUpstreamMode(SET_UPSTREAM)
                .setStartPoint(branch.name)
        }

        checkout.call()
    }

    fun verifyCurrent() {
        if (!clean) throw GradleException("Branch ${repository.fullBranch} must be clean before the release process can continue")

        fetchLatest()
        pullChanges()
    }

    fun pushChanges() {
        if (!hasRemote) return
        git.push()
            .setRemote(remote)
            .call()
    }

    fun pushTags() {
        if (!hasRemote) return
        git.push()
            .setRemote(remote)
            .setPushTags()
            .call()
    }

    fun tag(name: String, message: String) {
        git.tag()
            .setName(name)
            .setMessage(message)
            .call()
    }

    fun tags(): List<String> = git.tagList().call().map { it.name }

    private fun pullChanges() {
        if (!hasRemote) return
        if (!remoteBranches().any { shortenRefName(it.name) == shortenRefName(current.name) }) return
        git.pull()
            .setRemote(remote)
            .call()
    }

    private fun fetchLatest() {
        if (!hasRemote) return
        git.fetch()
            .setRemote(remote)
            .call()
    }

    private fun merge(source: Ref, target: Ref) {
        checkoutBranch(target)

        git.merge()
            .include(source)
            .setFastForward(MergeCommand.FastForwardMode.FF_ONLY)
            .setSquash(false)
            .setCommit(false)
            .call()
    }

    private fun createBranch(name: String): Ref {
        return git.branchCreate()
            .setName(name)
            .call()
    }

    private fun deleteBranch(branch: Ref) {
        git.branchDelete()
            .setBranchNames(branch.name)
            .setForce(true)
            .call()
    }

    private fun getOrCreateBranch(name: String): Ref = getBranch(name) ?: createBranch(name)

    private fun getBranch(name: String): Ref? = localBranches().findByName(name) ?: remoteBranches().findByName(name)

    private fun localBranches(): List<Ref> {
        return git.branchList().call()
    }

    private fun remoteBranches(): List<Ref> {
        if (!hasRemote) return emptyList()

        return git.lsRemote()
            .setRemote(remote)
            .call()
            .filter { it.name.startsWith("ref/heads") }
    }

    private fun getRemoteList(): List<RemoteConfig> = git.remoteList().call()

    private fun <R : Ref> List<R>.findByName(name: String): R? = this.firstOrNull { shortenRefName(it.name) == name }
}
