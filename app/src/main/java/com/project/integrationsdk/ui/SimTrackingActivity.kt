package com.project.integrationsdk.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.ActivitySimTrackingBinding
import com.project.integrationsdk.simtracker.SimCardAdapter
import com.project.integrationsdk.simtracker.OperatorConfig
import com.project.integrationsdk.simtracker.SimViewModel

class SimTrackingActivity : AppCompatActivity() {

    lateinit var binding: ActivitySimTrackingBinding
    private val viewModel: SimViewModel by viewModels()
    private lateinit var adapter: SimCardAdapter

    // Views
    private lateinit var rvSimCards: RecyclerView
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnRefresh: Button
    private lateinit var tvModeLabel: TextView

    // Permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.loadAndTrackSims()
            } else {
                tvStatus.text = "⚠️ READ_PHONE_STATE permission denied.\n" +
                        "Cannot read SIM information without this permission.\n\n" +
                        "Please grant it in App Settings and re-launch."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupRecyclerView()
        observeViewModel()
        showModeLabel()
        checkPermissionAndLoad()
    }

    private fun initViews() {
        rvSimCards = binding.rvSimCards
        tvStatus = binding.tvStatus
        progressBar = binding.progressBar
        btnRefresh = binding.btnRefresh
        tvModeLabel = binding.tvModeLabel

        btnRefresh.setOnClickListener {
            checkPermissionAndLoad()
        }
    }

    private fun setupRecyclerView() {
        adapter = SimCardAdapter()
        rvSimCards.layoutManager = LinearLayoutManager(this)
        rvSimCards.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnRefresh.isEnabled = !loading
        }

        viewModel.simClassifications.observe(this) { classifications ->
            adapter.submitList(classifications)
        }

        viewModel.statusMessage.observe(this) { message ->
            tvStatus.text = message
        }
    }

    private fun showModeLabel() {
        val mode = if (OperatorConfig.IS_POC_MODE)
            "🟡 POC MODE — India (Jio / Competitors: Airtel, Vi, BSNL)"
        else
            "🟢 PRODUCTION MODE — Ooredoo Markets"
        tvModeLabel.text = mode
    }

    private fun checkPermissionAndLoad() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.loadAndTrackSims()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE) -> {
                tvStatus.text = "This app needs READ_PHONE_STATE to identify SIM cards.\n" +
                        "Tap Refresh to request permission again."
                requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            }
        }
    }
}