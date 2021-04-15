package com.headlessideas.util

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import java.io.File

fun initGitRepository(directory: File): Git = Git.init().setDirectory(directory).setInitialBranch("develop").call()

fun Git.addAndCommit(vararg files: String) {
    val add = add()
    for (file in files) {
        add.addFilepattern(file)
    }
    add.call()
    commit().setMessage("Automatic commit").call()
}

fun Git.tags(): List<String> = tagList().call().map { Repository.shortenRefName(it.name) }
