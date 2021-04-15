package com.headlessideas.release.util

import org.ajoberstar.reckon.core.Version

class DelayedVersion(private val versionSupplier: () -> Version) {
    override fun toString(): String {
        return versionSupplier.invoke().toString()
    }
}
