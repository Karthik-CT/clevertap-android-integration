package com.project.integrationsdk.ui

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.ActivityCoachMarkBinding

class CoachMarkActivity : AppCompatActivity() {

    lateinit var binding: ActivityCoachMarkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoachMarkBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}