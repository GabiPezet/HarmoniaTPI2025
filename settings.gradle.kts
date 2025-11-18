pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven(uri("https://artifacts.mercadolibre.com/repository/android-releases"))
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = uri("https://artifacts.mercadolibre.com/repository/android-releases"))
        maven( "https://jitpack.io")
        maven{
            name = "TarsosDSP repository"
            url = uri("https://mvn.0110.be/releases")
        }
    maven(url = uri("https://artifacts.mercadolibre.com/repository/android-releases"))
    }
}

rootProject.name = "HarmoniaTPI"
include(":app")
 