pluginManagement {
    repositories {
//        maven { setUrl("https://maven.aliyun.com/repository/public/") }
//        maven { setUrl("https://jitpack.io") }
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://maven.scijava.org/content/repositories/public/") }
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven { setUrl("https://maven.aliyun.com/repository/public/") }
//        maven { setUrl("https://jitpack.io") }
        google()
        mavenCentral()
        maven { setUrl("https://maven.scijava.org/content/repositories/public/") }
        mavenLocal()
    }
}

rootProject.name = "InstallerX"
include(
    ":app",
    ":hidden-api"
)
