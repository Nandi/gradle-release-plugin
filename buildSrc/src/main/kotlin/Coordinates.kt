object PluginCoordinates {
    const val ID = "com.headlessideas.release.plugin"
    const val GROUP = "com.headlessideas.release"
    const val IMPLEMENTATION_CLASS = "com.headlessideas.release.ReleasePlugin"
}

object MavenCoordinates {
    const val GROUP = "com.headlessideas"
    const val ID = "gradle-release-plugin"
}

object PluginBundle {
    const val VCS = "https://github.com/nandi/gradle-release-plugin"
    const val WEBSITE = "https://github.com/nandi/gradle-release-plugin"
    const val DESCRIPTION = "Opinionated release plugin for gradle"
    const val DISPLAY_NAME = "Gradle release plugin"
    val TAGS = listOf(
        "plugin",
        "gradle",
        "release",
        "git"
    )
}

