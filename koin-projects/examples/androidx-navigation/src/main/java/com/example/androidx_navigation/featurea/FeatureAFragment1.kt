package com.example.androidx_navigation.featurea

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidx_navigation.AppScopeDep
import com.example.androidx_navigation.MainActivityScopeDep
import com.example.androidx_navigation.R
import com.example.androidx_navigation.featureb.FeatureBDependency
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

private val featureAModule = module { single { FeatureADependency(get()) } }

private val loadKoinModules by lazy { loadKoinModules(featureAModule) }

fun injectFeatureA() = loadKoinModules

class FeatureAFragment1 : Fragment(R.layout.fragment_feature_a_1) {

    private val featureADependency: FeatureADependency by inject()
    //Feature A should not be able to inject FeatureBDependency

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectFeatureA()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button).setOnClickListener {
            findNavController().navigate(R.id.action_featureAFragment1_to_featureAFragment2)
        }
        Log.d("Koin FeatureAFragment1", featureADependency.toString())
    }

    override fun onDestroyView() {
        //unload Koin here?
        super.onDestroyView()
    }

    override fun onDetach() {
        // unload Koin here?
        super.onDetach()
    }
}
