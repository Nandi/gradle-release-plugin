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
        kotlinScript { api ->
            val stage = if (api.gitBranch() != "main".branch()) {
                "-Preckon.stage=rc"
            } else ""
            api.gradlew("publishSpaceMavenPublicationToSpaceMavenRepository", stage)
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
            api.gradlew("publishPlugins")
        }
    }
}

fun String.branch() = "refs/heads/$this"
