object Versions {
    const val KTLINT = "0.41.0"
    const val JUNIT = "5.7.2"
    const val RECKON = "0.13.0"
    const val JGIT = "5.11.1.202105131744-r"
}

object BuildPluginsVersion {
    const val DETEKT = "1.17.0"
    const val KOTLIN = "1.5.0"
    const val KTLINT = "10.0.0"
    const val VERSIONS_PLUGIN = "0.38.0"
    const val RECKON_PLUGIN = "0.13.0"
    const val PLUGIN_PUBLISH = "0.14.0"
}

object VersioningLib {
    const val RECKON_CORE = "org.ajoberstar.reckon:reckon-core:${Versions.RECKON}"
    const val JGIT = "org.eclipse.jgit:org.eclipse.jgit:${Versions.JGIT}"
    const val JGIT_APACHE = "org.eclipse.jgit:org.eclipse.jgit.ssh.apache:${Versions.JGIT}"
}

object TestingLib {
    const val JUNIT_BOM = "org.junit:junit-bom:${Versions.JUNIT}"
    const val JUNIT_JUPITER = "org.junit.jupiter:junit-jupiter"
}