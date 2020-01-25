@file:Suppress("UnstableApiUsage")

import org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

apply(from = rootDir.resolve("gradle/target-common.gradle.kts"))

apply<KotlinMultiplatformPluginWrapper>()

val kotlinVersion: String by extra
val junitVersion: String by extra
val mockitoVersion: String by extra
val coroutinesVersion: String by extra

configure<KotlinMultiplatformExtension> {
    jvm {
        sourceSets {
            named("jvmMain") {
                dependencies {
                    api(kotlin("stdlib-jdk8", kotlinVersion))
                }
            }

            named("jvmTest") {
                dependencies {
                    api("junit:junit:$junitVersion")
                    api("org.mockito:mockito-inline:$mockitoVersion")

                    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                }
            }
        }
    }
}

apply(from = rootDir.resolve("gradle/checkstyle.gradle.kts"))
apply(from = rootDir.resolve("gradle/dokka.gradle.kts"))
apply(from = rootDir.resolve("gradle/publish.gradle.kts"))