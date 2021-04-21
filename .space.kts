job("Build and verify") {
    startOn {
        gitPush {
            branchFilter {
                -"develop".branch()
                -"main".branch()
            }
        }
    }
    container(displayName = "build, test and lint check", image = "openjdk:16-alpine") {
        kotlinScript { api ->
            api.gradlew("preMerge")
        }
    }
}

job("publish to maven") {
    startOn {
        gitPush {
            branchFilter {
                +"develop".branch()
                +"main".branch()
            }
        }
    }
    container(displayName = "build, test and lint check", image = "openjdk:16-alpine") {
        kotlinScript { api ->
            api.gradlew("preMerge")
        }
    }

    container(displayName = "Run publish script", image = "openjdk:16-alpine") {
        env["GRADLE_PUBLISH_KEY"] = Secrets("gradle_publish_key")
        env["GRADLE_PUBLISH_SECRET"] = Secrets("gradle_publish_secret")

        kotlinScript { api ->
            val stage = if (api.gitBranch() == "main".branch()) {
                "final"
            } else {
                "rc"
            }
            api.gradlew("publishSpaceMavenPublicationToSpaceMavenRepository", "-Preckon.stage=$stage")
        }
    }
}

job("publish to gradle plugin repository") {
    startOn {
        gitPush {
            branchFilter {
                +"main".branch()
            }
        }
    }
    container(displayName = "build, test and lint check", image = "openjdk:16-alpine") {
        kotlinScript { api ->
            api.gradlew("preMerge")
        }
    }

    container(displayName = "Run publish script", image = "openjdk:16-alpine") {
        env["GRADLE_PUBLISH_KEY"] = Secrets("gradle_publish_key")
        env["GRADLE_PUBLISH_SECRET"] = Secrets("gradle_publish_secret")

        kotlinScript { api ->
            api.gradlew("publishPlugins", "-Preckon.stage=final")
        }
    }
}

fun String.branch() = "refs/heads/$this"
