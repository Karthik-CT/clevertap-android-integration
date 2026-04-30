package com.project.integrationsdk.simtracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.project.integrationsdk.simtracker.SimInfo

/**
 * Reads all active SIM subscriptions from the device using SubscriptionManager.
 *
 * Requires READ_PHONE_STATE permission.
 * On dual-SIM devices, returns up to 2 SimInfo objects ordered by slot index.
 */
object SimReader {

    fun readSims(context: Context): List<SimInfo> {
        if (!hasPhonePermission(context)) {
            return emptyList()
        }

        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
                as? SubscriptionManager ?: return emptyList()

        val activeSubscriptions: List<SubscriptionInfo> =
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    subscriptionManager.activeSubscriptionInfoList ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: SecurityException) {
                emptyList()
            }

        return activeSubscriptions
            .sortedBy { it.simSlotIndex }
            .map { subInfo -> buildSimInfo(context, subInfo) }
    }

    private fun buildSimInfo(context: Context, subInfo: SubscriptionInfo): SimInfo {
        val slotIndex = subInfo.simSlotIndex
        val operatorName = subInfo.carrierName?.toString()?.trim() ?: ""
        val mccMnc = buildMccMnc(subInfo)
        val countryIso = subInfo.countryIso ?: ""

        // Get roaming status via TelephonyManager for this subscription
        val isRoaming = getIsRoaming(context, subInfo)

        // Roaming hard coded
//        val isRoaming = if (slotIndex == 1) true else getIsRoaming(context, subInfo)

        val simState = getSimState(context, slotIndex)

        return SimInfo(
            slotIndex = slotIndex,
            operatorName = operatorName,
            mccMnc = mccMnc,
            countryIso = countryIso,
            simState = simState,
            isRoaming = isRoaming
        )
    }

    private fun buildMccMnc(subInfo: SubscriptionInfo): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ has getMccString / getMncString
            val mcc = subInfo.mccString ?: ""
            val mnc = subInfo.mncString ?: ""
            "$mcc$mnc"
        } else {
            @Suppress("DEPRECATION")
            "${subInfo.mcc}${subInfo.mnc}"
        }
    }

    private fun getIsRoaming(context: Context, subInfo: SubscriptionInfo): Boolean {
        return try {
            val tm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.getSystemService(TelephonyManager::class.java)
                    ?.createForSubscriptionId(subInfo.subscriptionId)
            } else {
                context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            }
            tm?.isNetworkRoaming ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun getSimState(context: Context, slotIndex: Int): Int {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            ?: return TelephonyManager.SIM_STATE_UNKNOWN
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tm.getSimState(slotIndex)
        } else {
            tm.simState
        }
    }

    fun hasPhonePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
}