import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm") version BuildPluginsVersion.KOTLIN
    id("io.gitlab.arturbosch.detekt") version BuildPluginsVersion.DETEKT
    id("com.github.ben-manes.versions") version BuildPluginsVersion.VERSIONS_PLUGIN
    id("org.ajoberstar.reckon") version BuildPluginsVersion.RECKON_PLUGIN

    id("com.gradle.plugin-publish") version BuildPluginsVersion.PLUGIN_PUBLISH
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.headlessideas"

reckon {
    calcScopeFromProp()
    stages("rc", "final")
    calcStageFromProp()
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(gradleApi())
    implementation(Versioning.reckonCore)
    implementation(Versioning.jgit) {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(Versioning.jgitApache)

    testImplementation(platform(Junit.bom))
    testImplementation(Junit.jupiter)

    testImplementation(Kotest.core)
    testImplementation(Kotest.runner)
    testImplementation(Testing.mockk)
    testImplementation(Testing.strikt)
}


kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    website.set(PluginBundle.WEBSITE)
    vcsUrl.set(PluginBundle.VCS)
    plugins {
        create(PluginCoordinates.ID) {
            group = PluginCoordinates.GROUP
            id = PluginCoordinates.ID
            displayName = PluginBundle.DISPLAY_NAME
            description = PluginBundle.DESCRIPTION
            implementationClass = PluginCoordinates.IMPLEMENTATION_CLASS
            tags.set(PluginBundle.TAGS)
        }
    }
}

val spaceUsername: String? by project
val spacePassword: String? by project

publishing {
    publications {
        create<MavenPublication>("spaceMaven") {
            group = MavenCoordinates.GROUP
            artifactId = MavenCoordinates.ID
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "spaceMaven"
            url = uri("https://maven.pkg.jetbrains.space/headlessideas/p/grp/maven-snapshot")
            credentials {
                username = spaceUsername ?: System.getenv("JB_SPACE_CLIENT_ID")
                password = spacePassword ?: System.getenv("JB_SPACE_CLIENT_SECRET")
            }
        }
    }
}

detekt {
    config = rootProject.files("config/detekt/detekt.yml")
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

fun isNonStable(version: String) = "^[0-9,.v-]+(-r)?$".toRegex().matches(version).not()

tasks.register("reformatAll") {
    description = "Reformat all the Kotlin Code"

    dependsOn("ktlintFormat")
}

tasks.register("preMerge") {
    description = "Runs all the tests/verification tasks on both top level and included build."

    dependsOn("check")
    dependsOn("validatePlugins")
}

tasks.register("printVersion") {
    println(project.version)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.create("setupPluginUploadFromEnvironment") {
    doLast {
        val key = System.getenv("GRADLE_PUBLISH_KEY")
        val secret = System.getenv("GRADLE_PUBLISH_SECRET")

        if (key == null || secret == null) {
            throw GradleException("gradlePublishKey and/or gradlePublishSecret are not defined environment variables")
        }

        System.setProperty("gradle.publish.key", key)
        System.setProperty("gradle.publish.secret", secret)
    }
}

tasks.publishPlugins.orNull?.dependsOn("setupPluginUploadFromEnvironment")
