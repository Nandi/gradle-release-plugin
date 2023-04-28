# Gradle release plugin

The `gradle-release-plugin` is an opinionated versioning and release plugin using the strength of [Reckon](https://github.com/ajoberstar/reckon) and [jgit](https://www.eclipse.org/jgit/)
that requires minimal setup.

## Usage

### Adding plugin

To enable the plugin, add it to the top level build script You can add this plugin to your top-level build script using the following configuration:

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
    id("com.headlessideas.release.plugin") version "$version"
}
```

</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id "com.headlessideas.release.plugin" version "$version"
}
```

</details>

### Configuration

Configuration happens via the `releaseConfig` block:

```kotlin
 releaseConfig {
    mainBranch = "master"
    developBranch = "develop"
    releaseBranchPrefix = "release"
    releaseScopes = mutableListOf("rc", "final")
    remoteName = "origin"
    tagPrefix = "subproject-v"
}
```

*Note:* `releaseScopes` must contain the value `final`.

In addition, there are two properties that can be configured via the command line interface.

* `release.scope` defines the scope of the version numbering. Possible values are `major`, `minor` and `patch`
* `release.stage` defines the version stage. Possible values are the same as defined in the `releaseScopes` shown above, default values are `rc` and `final`

## Tasks

### **`printVersion`**

Displays the current version. The version is inferred by the current stage supplied by the `release.scope` property

There are three types of stages:

| Type              | Scheme                                                                | Example                | Description                                                                                                                     |
| ----------------- | --------------------------------------------------------------------- | ---------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| **final**         | `<major>.<minor>.<patch>`                                             | `1.2.3`                | A version ready for end-user consumption                                                                                        |
| **significant**   | `<major>.<minor>.<patch>-<stage>.<num>`                               | `1.3.0-rc.1`           | A version indicating an important stage has been reached on the way to the next final release (e.g. alpha, beta, rc, milestone) |
| **insignificant** | `<major>.<minor>.<patch>-<stage>.<num>.<commits>+<hash or timestamp>` | `1.3.0-rc.1.8+3bb4161` | A general build in-between significant releases.                                                                                |

- `<major>` a positive integer incremented when incompatible API changes are made
- `<minor>` a positive integer incremented when functionality is added while preserving backwards-compatibility
- `<patch>` a positive integer incremented when fixes are made that preserve backwards-compatibility
- `<stage>` an alphabetical identifier indicating a level of maturity on the way to a final release. They should make logical sense to a human, but alphabetical order **must** be the indicator of maturity to ensure they sort correctly. (
  e.g. milestone, rc, snapshot would not make sense because snapshot would sort after rc)
- `<num>` a positive integer incremented when a significant release is made
- `<commits>` a positive integer indicating the number of commits since the last final release was made
- `<hash or timestamp>` if the repo is clean, an abbreviated commit hash of the current HEAD, otherwise a UTC timestamp

To read more about how the version is inferred and behaves depending on the current state of the GIT repository, read [how Reckon works](https://github.com/ajoberstar/reckon/blob/main/docs/index.md)

### **`checkRepository`**

Checks that the main and develop branch are clean and updates them with the latest changes.

### **`release`**

The release process is divided into **five** steps:

#### Creating the release branch

The plugin creates a new release branch with the pattern `<releasePrefix>/<version>`, e.g. `release/1.0.0`, from the `develop` branch. Next tries to merge the `main` branch into the `release` branch.

#### Merge `release` into `main` branch

The plugin now checkout the `main` branch (or creates it if it does not exist) and merges the changes from the `release` branch into the `main` branch.

#### Tag commit with version

Using the inferred version calculated by [Reckon](https://github.com/ajoberstar/reckon), the plugin tags the current commit with it.

#### Push to remote

The plugin makes sure that the `main` branch is clean and up to date before pushing the changes to the remote repository.

#### Clean up

Before finishing, the plugin deletes the release branch.

## Examples

#### Print inferred major release version

```shell
gradle printVersion -Prelease.scope=major -Prelease.stage=final
2.0.0
```

The `scope` is set to `major` so that the most significant version number is incremented, and the `stage` is set to `final` so that the version is mark as the next release. **Note:** This only works on a clean repository

#### Print next release candidate version

```shell
gradle printVersion -Prelease.stage=rc
1.1.0-rc.1
```

The `scope` is omitted here, so the minor version number is incremented. The `stage` is set to `rc` to print the next Release Candidate. **Note:** This only works on a clean repository

#### Print the insignificant version

```shell
gradle printVersion
1.1.0-rc.1.8+3bb4161
```

For more information on how this version number is derived and composed read [how Reckon works](https://github.com/ajoberstar/reckon/blob/main/docs/index.md)