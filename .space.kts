job("Build and verify") {
    container(displayName = "build, test and lint check", image = "openjdk:16-alpine") {
        kotlinScript { api ->
            api.gradlew("preMerge")
        }
    }
}

job("publish") {
    startOn {
        gitPush {
            branchFilter {
                +"develop".branch()
                +"main".branch()
            }
        }
    }

    container(displayName = "Run publish script", image = "openjdk:16-alpine") {
        env["GRADLE_PUBLISH_KEY"] = Secrets("gradle_publish_key")
        env["GRADLE_PUBLISH_SECRET"] = Secrets("gradle_publish_secret")

        kotlinScript { api ->
            val stage = if (api.gitBranch() == "develop".branch()) {
                "rc"
            } else {
                "final"
            }
            api.gradlew("publishSpaceMavenPublicationToSpaceMavenRepository", "-Preckon.stage=$stage")
            api.gradlew("setupPluginUploadFromEnvironment")
            api.gradlew("publishPlugins", "-Preckon.stage=$stage")
        }
    }
}

fun String.branch() = "refs/heads/$this"