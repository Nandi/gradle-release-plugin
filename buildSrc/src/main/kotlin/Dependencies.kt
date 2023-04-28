object Versions {
    const val JUNIT = "5.9.2"
    const val RECKON = "0.18.0"
    const val JGIT = "6.5.0.202303070854-r"
    const val KOTEST = "5.5.5"
    const val MOCKK = "1.13.4"
    const val STRIKT = "0.34.1"
}

object BuildPluginsVersion {
    const val DETEKT = "1.22.0"
    const val KOTLIN = "1.8.21"
    const val VERSIONS_PLUGIN = "0.46.0"
    const val RECKON_PLUGIN = "0.18.0"
    const val PLUGIN_PUBLISH = "1.2.0"
}

object Versioning {
    const val reckonCore = "org.ajoberstar.reckon:reckon-core:${Versions.RECKON}"
    const val jgit = "org.eclipse.jgit:org.eclipse.jgit:${Versions.JGIT}"
    const val jgitApache = "org.eclipse.jgit:org.eclipse.jgit.ssh.apache:${Versions.JGIT}"
}

object Junit {
    const val bom = "org.junit:junit-bom:${Versions.JUNIT}"
    const val jupiter = "org.junit.jupiter:junit-jupiter"
}

object Testing {
    const val mockk = "io.mockk:mockk:${Versions.MOCKK}"
    const val strikt = "io.strikt:strikt-core:${Versions.STRIKT}"
}

object Kotest {
    const val core = "io.kotest:kotest-framework-datatest:${Versions.KOTEST}"
    const val runner = "io.kotest:kotest-runner-junit5-jvm:${Versions.KOTEST}"
}