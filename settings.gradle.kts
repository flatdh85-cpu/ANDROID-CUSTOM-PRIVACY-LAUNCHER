pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "minimal-os"
include(
    ":app-launcher",
    ":core-device",
    ":core-profiles",
    ":core-journal",
    ":core-debloat",
    ":core-recovery",
)
