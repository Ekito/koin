
# How to run 
run `:coffee-maker` `CoffeeAppTest`. It should compile the new gradle module `koin-ksp` accordingly.

Note that sometimes when editing files it's necessary to clean the build. There are ways to associate input kt files -> output kt files but that's not done yet.

# Overview
This POC covers generating a single module with a number of `single{}` and `factory{}` definitions.

## Files added to this branch

- TestProcessor is the entry point to a new ksp plugin
- InjectorWriter helps generate ??
- KoinVisitor is a class that inherits `KSTopDownVisitor`, and can be used to scrutinize and filter classes and fields that `TestProcessor` is interested in. In this branch, we're interested in anything that has the `@KoinInject` annotation
- KoinInject is an annotation for classes and fields. When used to annotate a class it indicates we want a factory/single for it. When used in a field it also indicates we want a factory/single for it (to help with `checkModules` later on). 
- CoffeeAppTest runs a unit test with the generated module

## Generated files

- `KoinKspModule.kt` contains a single module with all factories and singletons sufficient to cover the whole koin dependency graph.
- `CoffeeAppKspInjector.kt` is used to help inject `CoffeeApp::maker2`, see `CoffeeApp::init`
- See logs at `examples/coffee-maker/build/generated/ksp/main/resources/`


Notes:
- `resolver.getSymbolsWithAnnotation(KoinInject::class.java.canonicalName)` is able to find anything annotated with `@KoinInject`
- `KoinVisitor` will call `lambdaClass` or `lambdaProperty` based on if the symbol is class or property
- during `TestProcessor::process()` we will generate a `Set` of classes it's interested in and a `Set` of properties it's interested in
- during `TestProcessor::finish()` we will write the generated kotlin source code

### Algorithm 1 (KoinKspModule)

Step 1:
- We find all classes annotated with `@KoinInject`
- We generate a `single` for each of them (we can provide a parameter later for choosing either single or factory)
- We find all supertypes of those classes and generate a `factory<Super> { get<Type>() } ` for each of them

### Algorithm 2 (CoffeeAppKspInjector):

The graph generation starts with a leaf of the final graph, and proceeds 

- TestProcessor make a list fields annotated with `@KoinInject`,
- it will find `CoffeeApp.maker2`, and will pull its declared class `CoffeeMaker`
- it will generate `CoffeeAppKspInjector::inject(injectee: CoffeeApp)`
- its contents will do `injectee.maker2 = injectee.getKoin().get()`     
- `CoffeeApp` does  ` init { CoffeeAppKspInjector().inject(this) } `  

