job("Build and publish") {
    gradlew("openjdk:15-alpine", "preMerge")
    container(displayName = "Run publish script", image = "openjdk:15-alpine") {
        kotlinScript { api ->
            if (api.gitBranch() == "refs/heads/develop") {
                api.gradlew("publishSpaceMavenPublicationToSpaceMavenRepository -Preckon.stage=rc")
            }
        }
    }
}
