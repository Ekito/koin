package com.example.androidx_navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

private val activityModule = module { single { MainActivityScopeDep(get()) } }

private val loadKoinModules by lazy { loadKoinModules(activityModule) }

fun injectMainActivity() = loadKoinModules

// Declare a simple dependency that is created in the MainActivity and should only exist as long as the MainActivity is alive.
class MainActivityScopeDep(private val appScopeDep: AppScopeDep)

class MainActivity : AppCompatActivity() {

    private val mainActivityScopeDep: MainActivityScopeDep by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        injectMainActivity()
        Log.d("Koin MainActivity", mainActivityScopeDep.toString())
    }

    override fun onDestroy() {
        // When to unload activity dependencies?
        //unloadKoinModules(activityModule)
        super.onDestroy()
    }
}
