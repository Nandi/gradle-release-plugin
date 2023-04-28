package com.headlessideas.release.util

import org.ajoberstar.reckon.core.Reckoner
import org.ajoberstar.reckon.core.Scope
import org.ajoberstar.reckon.core.ScopeCalculator
import org.ajoberstar.reckon.core.Version
import org.eclipse.jgit.lib.Repository
import java.util.*

@Suppress("SpreadOperator")
class VersionService(
    repo: Repository,
    stages: List<String>,
    stage: String?,
    scope: String?,
) {

    private val reckoner: Reckoner

    init {
        val scopeCalculator = ScopeCalculator.ofUserString { _ -> scope.toOptional() }

        reckoner = Reckoner.builder()
            .git(repo)
            .scopeCalc(scopeCalculator)
            .defaultInferredScope(Scope.MINOR)
            .stages(*stages.toTypedArray())
            .stageCalc { _, _ -> stage.toOptional() }
            .build()
    }

    fun version(): Version {
        return reckoner.reckon()
    }

    private fun <T : Any> T?.toOptional(): Optional<T> = Optional.ofNullable(this)
}
