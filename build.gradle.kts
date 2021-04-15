import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm") version BuildPluginsVersion.KOTLIN
    id("io.gitlab.arturbosch.detekt") version BuildPluginsVersion.DETEKT
    id("org.jlleitschuh.gradle.ktlint") version BuildPluginsVersion.KTLINT
    id("com.github.ben-manes.versions") version BuildPluginsVersion.VERSIONS_PLUGIN
    id("org.ajoberstar.reckon") version BuildPluginsVersion.RECKON_PLUGIN

    id("com.gradle.plugin-publish") version BuildPluginsVersion.PLUGIN_PUBLISH
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.headlessideas"

reckon {
    scopeFromProp()
    stageFromProp("rc", "final")
}

repositories {
    google()
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(gradleApi())
    implementation(VersioningLib.RECKON_CORE)
    implementation(VersioningLib.JGIT) {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(VersioningLib.JGIT_APACHE)

    testImplementation(platform(TestingLib.JUNIT_BOM))
    testImplementation(TestingLib.JUNIT_JUPITER)
}

gradlePlugin {
    plugins {
        create(PluginCoordinates.ID) {
            group = PluginCoordinates.GROUP
            id = PluginCoordinates.ID
            implementationClass = PluginCoordinates.IMPLEMENTATION_CLASS
        }
    }
}

// Configuration Block for the Plugin Marker artifact on Plugin Central
pluginBundle {
    website = PluginBundle.WEBSITE
    vcsUrl = PluginBundle.VCS
    description = PluginBundle.DESCRIPTION
    tags = PluginBundle.TAGS

    plugins {
        getByName(PluginCoordinates.ID) {
            displayName = PluginBundle.DISPLAY_NAME
        }
    }
}

val spaceUsername: String by project
val spacePassword: String by project

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
                username = spaceUsername
                password = spacePassword
            }
        }
    }
}

ktlint {
    debug.set(false)
    version.set(Versions.KTLINT)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    enableExperimentalRules.set(true)
    disabledRules.add("no-wildcard-imports")
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

detekt {
    config = rootProject.files("config/detekt/detekt.yml")
    reports {
        html {
            enabled = true
            destination = file("build/reports/detekt.html")
        }
    }
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

    dependsOn(":check")
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
