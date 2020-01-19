import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

apply(rootDir.resolve("gradle/target-android.gradle.kts"))

description = "Koin project - $name"

configure<KotlinMultiplatformExtension> {
    sourceSets {
        named("androidMain") {
            dependencies {
                api(project(":koin-core"))
            }
        }
    }
}
