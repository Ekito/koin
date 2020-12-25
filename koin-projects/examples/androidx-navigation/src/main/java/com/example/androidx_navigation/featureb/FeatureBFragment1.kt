package com.example.androidx_navigation.featureb

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidx_navigation.R
import com.example.androidx_navigation.featurea.FeatureADependency
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

private val featureBModule = module { single { FeatureBDependency(get()) } }

private val loadKoinModules by lazy { loadKoinModules(featureBModule) }

fun injectFeatureB() = loadKoinModules

class FeatureBFragment1 : Fragment(R.layout.fragment_feature_b_1) {

    //Feature B should not be able to inject FeatureADependency
    private val featureBDependency: FeatureBDependency by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectFeatureB()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button) .setOnClickListener {
            findNavController().navigate(R.id.action_featureBFragment1_to_featureBFragment2)
        }
        Log.d("Koin FeatureAFragment1", featureBDependency.toString())
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
