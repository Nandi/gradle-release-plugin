job("Build and verify") {
    container(displayName = "build, test and lint check", image = "openjdk:16-alpine") {
        kotlinScript { api ->
            api.gradlew("preMerge")
        }
    }
}

job("publish release candidate") {
    startOn {
        gitPush {
            branchFilter {
                +"ref/heads/develop"
            }
        }
    }

    container(displayName = "Run publish script", image = "openjdk:16-alpine") {
        kotlinScript { api ->
            api.gradlew("publishSpaceMavenPublicationToSpaceMavenRepository", "-Preckon.stage=rc")
        }
    }
}
