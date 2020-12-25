package com.example.androidx_navigation.featureb

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.androidx_navigation.R
import com.example.androidx_navigation.featurea.FeatureADependency
import org.koin.android.ext.android.inject

class FeatureBFragment2 : Fragment(R.layout.fragment_feature_b_2) {
    private val featureBDependency: FeatureBDependency by inject()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Koin FeatureBFragment2", featureBDependency.toString())
    }
}

