package com.project.integrationsdk.manager

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.QueryProductDetailsParams

class DcbPocManager(
    private val context: Context
) {

    private val billingClient: BillingClient =
        BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        println("DCB POC: Purchase flow completed")
                        purchases?.forEach {
                            println("DCB POC: Purchase = $it")
                        }
                    }

                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        println("DCB POC: User cancelled")
                    }

                    else -> {
                        println(
                            "DCB POC: Billing error = " +
                                    "${billingResult.responseCode} " +
                                    billingResult.debugMessage
                        )
                    }
                }
            }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .build()

    fun connect(onConnected: () -> Unit) {

        if (billingClient.isReady) {
            onConnected()
            return
        }

        billingClient.startConnection(
            object : BillingClientStateListener {

                override fun onBillingSetupFinished(
                    billingResult: BillingResult
                ) {

                    if (
                        billingResult.responseCode ==
                        BillingClient.BillingResponseCode.OK
                    ) {
                        println("DCB POC: Billing connected")
                        onConnected()
                    } else {
                        println(
                            "DCB POC: Billing connection failed: " +
                                    billingResult.debugMessage
                        )
                    }
                }

                override fun onBillingServiceDisconnected() {
                    println("DCB POC: Billing disconnected")
                }
            }
        )
    }

    fun launchPurchase(
        activity: Activity,
        productId: String
    ) {

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(
                    BillingClient.ProductType.INAPP
                )
                .build()
        )

        val params =
            QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

        billingClient.queryProductDetailsAsync(params) {
                billingResult,
                queryProductDetailsResult ->

            if (
                billingResult.responseCode !=
                BillingClient.BillingResponseCode.OK
            ) {

                println(
                    "DCB POC: Product query failed: " +
                            billingResult.debugMessage
                )

                return@queryProductDetailsAsync
            }

            val productDetails =
                queryProductDetailsResult.productDetailsList
                    .firstOrNull()

            if (productDetails == null) {

                println(
                    "DCB POC: Product not found: $productId"
                )

                return@queryProductDetailsAsync
            }

            val offer = productDetails.oneTimePurchaseOfferDetailsList?.firstOrNull()

            if (offer == null) {
                println(
                    "DCB POC: No eligible purchase offer"
                )
                return@queryProductDetailsAsync
            }

            val productDetailsParams = BillingFlowParams.ProductDetailsParams
                    .newBuilder()
                    .setProductDetails(productDetails)
                    .build()

            val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(productDetailsParams)
                    )
                    .build()

            val result = billingClient.launchBillingFlow(
                    activity,
                    billingFlowParams
                )

            println("DCB POC: launchBillingFlow = " + "${result.responseCode} " + result.debugMessage)
        }
    }

    fun isReady(): Boolean {
        return billingClient.isReady
    }

    fun destroy() {
        billingClient.endConnection()
    }
}