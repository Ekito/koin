
plugins {

    id("com.google.devtools.ksp") version "1.4.30-1.0.0-alpha02"
    kotlin("jvm")
}

dependencies{
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":koin-ksp"))
    ksp(project(":koin-ksp"))
}