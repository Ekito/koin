

## Starting Koin for Android

In any Android class:

```kotlin
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // use AndroidLogger as Koin Logger - default Level.INFO
            androidLogger()

            // use the Android context given there
            androidContext(this@MainApplication)

            // load properties from assets/koin.properties file
            androidFileProperties()

            // module list
            modules(offlineWeatherApp)
        }
    }
}
```

<div class="alert alert-primary" role="alert">
    if you can't inject android context or appliation, be sure to use androidContext() function in your Koin application declaration.
</div>

## Use the Android context

In your definition, you can inject `Context` or `Application` instance with `androidContext()` and `androidApplication()` functions:

```kotlin
module {
    single { MyAndroidComponent(androidContext()) }
}
```

## Android Components as KoinComponents

`Activity`, `Fragment` & `Service` are extended by Koin to be considered as `KoinComponents` out of the box:

```kotlin
class MyActivity : AppCompatActivity(){

    // Inject MyPresenter
    val presenter : MyPresenter by inject()

    override fun onCreate() {
        super.onCreate()

        // or directly retrieve instance
        val presenter : MyPresenter = get()
    }
}
```

Those classes can then use:

* `get()` or `by inject()` instance retrieving
* `getKoin()` to access th current `Koin` instance

If you need to inject dependencies from another class and can't declare it in a module, you can still tag it with `KoinComponent` interface.

## Extended Scope API 

> for Android (koin-android-scope or koin-androidx-scope projects)

Scope API is more close to the Android platform. Both `Activity` & `Fragment` have extensions for Scope API: `currentScope` get the current associated Koin scope. This scope is created & bound to the component's lifecycle.

You can use directly the associated Koin scope to retrieve components:

```kotlin
class DetailActivity : AppCompatActivity(), DetailContract.View {

    // inject from current activity scope instance
    override val presenter: DetailContract.Presenter by currentScope.inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        //...
    }
}
```

Easy to declare your Android component's scope:

```kotlin
module {
    // declare a scope for DetailActivity
    scope<DetailActivity> {
        scoped<DetailContract.Presenter> { DetailPresenter(get(), get()) }
    }
}
```

Any Activity or Fragment can use directly the scope API: `createScope()`, `getScope()` and `deleteScope()`.

## Android ViewModel

> (koin-android-viewmodel or koin-androidx-viewmodel projects)

Koin brings special features to manage ViewModel:

* `viewModel` special DSL keyword to declare a ViewModel
* `by viewModel()` & `getViewModel()` to inject ViewModel instance (from `Activity` & `Fragment`)
* `by sharedViewModel()` & `getSharedViewModel()` to reuse ViewModel instance from hosting Activity (from `Fragment`)

Let's declare a ViewModel in a module:

```kotlin
val myModule : Module = module {
    
    // ViewModel instance of MyViewModel
    // get() will resolve Repository instance
    viewModel { MyViewModel(get()) }

    // Single instance of Repository
    single<Repository> { MyRepository() }
}
```

Inject it in an Activity:

```kotlin
class MyActivity : AppCompatActivity(){

    // Lazy inject MyViewModel
    val model : MyViewModel by viewModel()

    override fun onCreate() {
        super.onCreate()

        // or also direct retrieve instance
        val model : MyViewModel = getViewModel()
    }
}
```

## Android Work Manager
> (koin-android-workmanager project)
>
### Configure Koin as Work Manager Factory 

Disable the default work manager factory in the manifest:
```xml
<manifest>

   <application>
      . . . 
      <provider
            android:name="androidx.work.impl.WorkManagerInitializer"
            android:authorities="${applicationId}.workmanager-init"
            tools:node="remove" />
   </application>
</manifest>    
```

Activate Koin's factory in Application:

```kotlin

  startKoin {
            ...
            androidContext(applicationContext)
            workManagerFactory()
        }
``` 

Note that `workManagerFactory()` must come after `androidContext(applicationContext)`, or the application will fail to initialize in the proper order and will crash.

### Declare worker listeners


Koin brings special features to manage ViewModel:

* `worker` special DSL keyword to declare an instance of ListenableWorker

Let's declare a ListenableWorker in a module:

```kotlin
val myModule : Module = module {
    
    // ViewModel instance of MyViewModel
    // get() will resolve Repository instance
    worker { MyWorker(get()) }

    // Single instance of Repository
    single<Repository> { MyRepository() }
}
```
 

### Start the worker as usual

```kotlin

            OneTimeWorkRequestBuilder<MyWorker>()
                .build()
                .let{
                    workManager.enqueue(it )
                }
 
```
### Troubleshooting

Problem: Crash with the following message
> E/WM-WorkerFactory: Could not instantiate xxx.xxx.xxxWorker
>  Caused by: java.lang.IllegalStateException: WorkManager is already initialized.  Did you try to initialize it manually without disabling WorkManagerInitializer? See WorkManager#initialize(Context, Configuration) or the class level Javadoc for more information.
>

Solution: add the declaration in manifest to disable default work manager factory

---------------

Problem: Crash with the following message 
> Caused by: java.lang.IllegalStateException: WorkManager is not initialized properly.  You have explicitly disabled WorkManagerInitializer in your manifest, have not manually called WorkManager#initialize at this point, and your Application does not implement Configuration.Provider.

Solution: Application is not installing koin's work manager factory ( `workManagerFactory()` statement is missing )

---------

Problem: Crash with the following message
>  java.lang.RuntimeException: Unable to create application xxx.xxx.xxxApplication: org.koin.core.error.NoBeanDefFoundException: No definition found for class:'android.content.Context'. Check your definitions!

Solution: while starting koin add `androidContext(applicationContext)` before `workManagerFactory()`

----------

Problem: Crash with the following message
> E/WM-DelegatingWkrFctry: Unable to instantiate a ListenableWorker (xxx.xxx.xxxWorker)
     org.koin.core.error.NoBeanDefFoundException: No definition found for class:'androidx.work.ListenableWorker' & qualifier:'xxx.xxx.xxxWorker'. Check your definitions!
>

Solution: add a definition for `xxxWorker` in one of the loaded modules, such as 

```kotlin
val myModule : Module = module {
    worker { xxxWorker(get()) }
}
```


