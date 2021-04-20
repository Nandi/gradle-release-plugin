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
                +"ref/heads/develop"
                +"ref/heads/main"
            }
        }
    }

    container(displayName = "Run publish script", image = "openjdk:16-alpine") {
        kotlinScript { api ->
            val stage = if (api.gitBranch() == "ref/heads/develop") {
                "rc"
            } else {
                "final"
            }

            val publishKey = Secrets("gradle_publish_key")
            val publishSecret = Secrets("gradle_publish_secret")

            api.gradlew("publishSpaceMavenPublicationToSpaceMavenRepository", "-Preckon.stage=$stage")
            api.gradlew("publishPlugins", "-Preckon.stage=$stage", "-Pgradle.publish.key=$publishKey", "-Pgradle.publish.secret=$publishSecret")
        }
    }
}
