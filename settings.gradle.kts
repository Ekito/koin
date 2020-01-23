rootProject.name = "koin"

// Core
include("koin-core")
include("koin-test")
// Core Sample
include("examples:coffee-maker")
include("examples:android-perfs")

// Core extended
include("koin-core-ext")

// Android
include("koin-android")
include("koin-android-scope", "koin-android-viewmodel")
include("koin-androidx-scope", "koin-androidx-fragment", "koin-androidx-viewmodel")

// Android Ext
include("koin-android-ext")
include("koin-androidx-ext")

// Android Samples
include("examples:android-samples")
include("examples:androidx-samples")

// Ktor
include("koin-logger-slf4j")
include("koin-ktor")
// Ktor Sample
include("examples:hello-ktor")
include("examples:multimodule-ktor:app")
include("examples:multimodule-ktor:common")
include("examples:multimodule-ktor:module-a")
include("examples:multimodule-ktor:module-b")