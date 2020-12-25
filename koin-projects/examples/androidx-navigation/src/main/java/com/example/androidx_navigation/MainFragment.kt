package com.example.androidx_navigation

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class MainFragment : Fragment(R.layout.fragment_main) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.featureAButton).setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_featureAFragment1)
        }

        view.findViewById<Button>(R.id.featureBButton).setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_featureBFragment1)
        }
    }
}
