package com.example.androidx_navigation.featurea

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.androidx_navigation.R
import org.koin.android.ext.android.inject

class FeatureAFragment2 : Fragment(R.layout.fragment_feature_a_2) {
    private val featureADependency: FeatureADependency by inject()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Koin FeatureAFragment2", featureADependency.toString())
    }
}
