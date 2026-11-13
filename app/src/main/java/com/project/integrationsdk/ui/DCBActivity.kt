package com.project.integrationsdk.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.ActivityDcbactivityBinding
import com.project.integrationsdk.manager.DcbPocManager

class DCBActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDcbactivityBinding
    private lateinit var dcbPocManager: DcbPocManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDcbactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<Button>(R.id.btnOpenPlayStore).setOnClickListener {
            openGooglePlay()
        }

        findViewById<Button>(R.id.btnPaymentMethods).setOnClickListener {
            openPaymentMethods()
        }

        findViewById<Button>(R.id.btnCarrierBilling).setOnClickListener {
            openCarrierBilling()
        }

        dcbPocManager = DcbPocManager(this)

        dcbPocManager.connect {
            println("DCB POC: Ready")
        }

        findViewById<Button>(R.id.btnDcbPoc)
            .setOnClickListener {
                dcbPocManager.launchPurchase(activity = this, productId = "dcb_test_product")
            }
    }

    override fun onDestroy() {
        dcbPocManager.destroy()
        super.onDestroy()
    }

    private fun openGooglePlay() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=com.android.vending")
        )

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Google Play Store is not available",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun openPaymentMethods() {
        val intentsToTry = listOf(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/paymentmethods")
            ),
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/account")
            )
        )

        launchFirstWorkingIntent(intentsToTry)
    }

    private fun openCarrierBilling() {
        /*
         * There is no officially documented public URI that guarantees
         * navigation directly to:
         *
         * Google Play
         *   -> Payment methods
         *   -> Carrier billing
         *   -> Ooredoo
         *
         * Therefore this method is intentionally kept as a POC.
         *
         * We can test known Google Play destinations and then determine
         * whether a more specific Play Store intent works on the device.
         */

        val intentsToTry = listOf(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/paymentmethods")
            ),
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://account")
            ),
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://paymentmethods")
            )
        )

        launchFirstWorkingIntent(intentsToTry)
    }

    private fun launchFirstWorkingIntent(intents: List<Intent>) {
        for (intent in intents) {
            try {
                startActivity(intent)
                return
            } catch (_: ActivityNotFoundException) {
                // Try next intent
            }
        }

        Toast.makeText(
            this,
            "No supported Google Play destination found",
            Toast.LENGTH_LONG
        ).show()
    }
}