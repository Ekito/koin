# Setup
## How to develop it it?
./gradlew :koin-ksp:build examples:coffee-maker:build --continuous

or ./gradlew examples:coffee-maker:test

- See source code files generated at `examples/coffee-maker/build/generated/ksp/main/java/`
- See logs at `examples/coffee-maker/build/generated/ksp/main/resources/`
- See tech details at KSP_Draft.md

## How to apply it in a koin module

```kotlin
dependencies {
    ...
        implementation(project(":koin-ksp"))
        ksp(project(":koin-ksp"))
    ...
}
```

