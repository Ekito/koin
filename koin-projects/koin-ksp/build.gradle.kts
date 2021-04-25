plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    google()
}

dependencies {

    implementation(project(":koin-core"))

    implementation(kotlin("stdlib"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.4.31-1.0.0-alpha06")
    implementation("com.squareup:javapoet:1.12.1")
}
