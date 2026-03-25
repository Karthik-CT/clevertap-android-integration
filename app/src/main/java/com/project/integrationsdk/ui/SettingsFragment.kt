//package com.project.integrationsdk.ui
//
//import android.content.Context
//import android.content.Intent
//import android.graphics.Color
//import android.graphics.drawable.GradientDrawable
//import android.os.Build
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.animation.DecelerateInterpolator
//import android.widget.PopupMenu
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import androidx.appcompat.app.AlertDialog
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.fragment.app.Fragment
//import com.clevertap.android.sdk.CleverTapAPI
//import com.project.integrationsdk.data.DashboardConfig
//import com.project.integrationsdk.data.UserPrefs
//import com.project.integrationsdk.databinding.FragmentSettingsBinding
//
//class SettingsFragment : Fragment() {
//
//    private var _binding: FragmentSettingsBinding? = null
//    private val binding get() = _binding!!
//
//    private val ct by lazy { CleverTapAPI.getDefaultInstance(requireContext() as Context) }
//    private var pendingDashboard: DashboardConfig.Dashboard? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ) = FragmentSettingsBinding.inflate(inflater, container, false)
//        .also { _binding = it }.root
//
//    @RequiresApi(Build.VERSION_CODES.P)
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        applyStatusBarInset()
//        setupDashboardSection()
//        setupMessagingToggles()
//        setupAccountSection()
//        setupAppInfo()
//        setupDangerZone()
//    }
//
//    override fun onDestroyView() {
//        _binding = null
//        super.onDestroyView()
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    //  STATUS BAR INSET
//    // ─────────────────────────────────────────────────────────────
//
//    private fun applyStatusBarInset() {
//        val extra = (14 * resources.displayMetrics.density).toInt()
//        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarContainer) { v, insets ->
//            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
//            v.setPadding(v.paddingLeft, top + extra, v.paddingRight, v.paddingBottom)
//            insets
//        }
//        ViewCompat.requestApplyInsets(binding.toolbarContainer)
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    //  SECTION 1: DASHBOARD SWITCHER
//    // ─────────────────────────────────────────────────────────────
//
//    private fun setupDashboardSection() {
//        val dashboards = DashboardConfig.dashboards
//        val active = DashboardConfig.getActive(requireContext())
//
//        // Set initial selected dashboard in the styled trigger
//        pendingDashboard = active
//        binding.tvSpinnerSelected.text = active.name
//        updateDashboardDisplay(active)
//
//        // Tap the styled trigger → show a PopupMenu (fully theme-aware, no black box)
//        binding.spinnerDashboard.setOnClickListener { anchor ->
//            val popup = PopupMenu(requireContext(), anchor)
//            dashboards.forEachIndexed { index, dashboard ->
//                popup.menu.add(0, index, index, dashboard.name)
//            }
//            popup.setOnMenuItemClickListener { item ->
//                val selected = dashboards[item.itemId]
//                pendingDashboard = selected
//                binding.tvSpinnerSelected.text = selected.name
//                updateDashboardDisplay(selected)
//                true
//            }
//            popup.show()
//        }
//
//        binding.btnSwitchDashboard.setOnClickListener { view ->
//            animatePress(view) {
//                val selected = pendingDashboard ?: DashboardConfig.getActive(requireContext())
//                val current = DashboardConfig.getActive(requireContext())
//                if (selected.id == current.id) {
//                    toast("Already on ${selected.name}")
//                    return@animatePress
//                }
//                AlertDialog.Builder(requireContext())
//                    .setTitle("Switch to ${selected.name}?")
//                    .setMessage("The app will restart and begin a new CleverTap session.\nAccount ID: ${selected.accountId}")
//                    .setPositiveButton("Switch & Restart") { _, _ ->
//                        DashboardConfig.setActive(requireContext(), selected)
//                        restartApp()
//                    }
//                    .setNegativeButton("Cancel", null)
//                    .show()
//            }
//        }
//    }
//
//    private fun updateDashboardDisplay(d: DashboardConfig.Dashboard) {
//        binding.tvAccountId.text = d.accountId
////        binding.tvToken.text = if (d.token.length > 4)
////            "•".repeat(d.token.length - 4) + d.token.takeLast(4)
////        else d.token
//        binding.tvToken.text = d.token
//        // Show region if present, hide row if null
//        if (d.region != null) {
//            binding.tvRegion.text = d.region.uppercase()
//            binding.rowRegion.visibility = View.VISIBLE
//        } else {
//            binding.rowRegion.visibility = View.GONE
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    //  SECTION 2: MESSAGING PREFERENCES
//    //  FIX: Direct view references from binding — no ItemToggleRowBinding.
//    //  FIX: HashMap cast issue — use mapOf which returns Map<String, Any>.
//    // ─────────────────────────────────────────────────────────────
//
//    private fun setupMessagingToggles() {
//        val profile = UserPrefs.load(requireContext())
//
//        // Apply icon tint + background colors programmatically
//        applyIconStyle(binding.iconBgEmail, binding.iconEmail, "#EDE9FD", "#5B4FCF")
//        applyIconStyle(binding.iconBgPush, binding.iconPush, "#FFE8E3", "#D9634F")
//        applyIconStyle(binding.iconBgSms, binding.iconSms, "#E2FAF4", "#00A878")
//        applyIconStyle(binding.iconBgWa, binding.iconWa, "#E2FAF4", "#00A878")
//
//        // Set initial states
//        binding.switchEmail.isChecked = profile.msgEmail
//        binding.switchPush.isChecked = profile.msgPush
//        binding.switchSms.isChecked = profile.msgSms
//        binding.switchWhatsapp.isChecked = profile.msgWhatsapp
//
//        // FIX: use mapOf<String, Any> — no explicit cast needed
//        binding.switchEmail.setOnCheckedChangeListener { _, checked ->
//            ct?.pushProfile(mapOf("MSG-email" to checked as Any))
//            UserPrefs.save(
//                requireContext(),
//                UserPrefs.load(requireContext()).copy(msgEmail = checked)
//            )
//        }
//        binding.switchPush.setOnCheckedChangeListener { _, checked ->
//            ct?.pushProfile(mapOf("MSG-push" to checked as Any))
//            UserPrefs.save(
//                requireContext(),
//                UserPrefs.load(requireContext()).copy(msgPush = checked)
//            )
//        }
//        binding.switchSms.setOnCheckedChangeListener { _, checked ->
//            ct?.pushProfile(mapOf("MSG-sms" to checked as Any))
//            UserPrefs.save(
//                requireContext(),
//                UserPrefs.load(requireContext()).copy(msgSms = checked)
//            )
//        }
//        binding.switchWhatsapp.setOnCheckedChangeListener { _, checked ->
//            ct?.pushProfile(mapOf("MSG-whatsapp" to checked as Any))
//            UserPrefs.save(
//                requireContext(),
//                UserPrefs.load(requireContext()).copy(msgWhatsapp = checked)
//            )
//        }
//    }
//
//    private fun applyIconStyle(
//        container: View,
//        icon: android.widget.ImageView,
//        bgHex: String,
//        tintHex: String
//    ) {
//        // Rounded square background
//        val bg = GradientDrawable().apply {
//            shape = GradientDrawable.RECTANGLE
//            cornerRadius = 10 * resources.displayMetrics.density
//            setColor(Color.parseColor(bgHex))
//        }
//        container.background = bg
//        icon.setColorFilter(Color.parseColor(tintHex))
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    //  SECTION 3: ACCOUNT
//    // ─────────────────────────────────────────────────────────────
//
//    private fun setupAccountSection() {
//        val profile = UserPrefs.load(requireContext())
//        binding.tvSignedInEmail.text = profile.email.ifEmpty { "—" }
//        binding.tvIdentity.text = profile.identity.ifEmpty { "—" }
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    //  SECTION 4: APP INFO
//    //  FIX: CleverTapAPI.SDKVersion doesn't exist — use the correct
//    //       constant com.clevertap.android.sdk.BuildConfig.SDK_VERSION_STRING
//    //       or just read it from the SDK's gradle dependency string.
//    // ─────────────────────────────────────────────────────────────
//
//    @RequiresApi(Build.VERSION_CODES.P)
//    private fun setupAppInfo() {
//        // App version
//        binding.tvAppVersion.text = try {
//            val pi = requireContext().packageManager
//                .getPackageInfo(requireContext().packageName, 0)
//            "${pi.versionName} (${pi.longVersionCode})"
//        } catch (e: Exception) {
//            "—"
//        }
//
//        // CleverTap Device ID — the __gq... value shown in CT dashboard
//        // This is the unique identifier CleverTap assigns to this device.
//        val deviceId = ct?.cleverTapID ?: "—"
//        binding.tvDeviceId.text = deviceId
//
//        // Tap to copy
//        binding.tvDeviceId.setOnClickListener {
//            val clipboard = requireContext()
//                .getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
//            clipboard.setPrimaryClip(
//                android.content.ClipData.newPlainText("CleverTap Device ID", deviceId)
//            )
//            toast("Device ID copied")
//        }
//
//        // Copy icon button
//        binding.btnCopyDeviceId.setOnClickListener {
//            val clipboard = requireContext()
//                .getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
//            clipboard.setPrimaryClip(
//                android.content.ClipData.newPlainText("CleverTap Device ID", deviceId)
//            )
//            toast("Device ID copied")
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    //  SECTION 5: DANGER ZONE
//    // ─────────────────────────────────────────────────────────────
//
//    private fun setupDangerZone() {
//        binding.btnClearData.setOnClickListener { view ->
//            animatePress(view) {
//                AlertDialog.Builder(requireContext())
//                    .setTitle("Clear Local Data?")
//                    .setMessage("Clears all cached profile data and dashboard selection. You will NOT be logged out.")
//                    .setPositiveButton("Clear") { _, _ ->
//                        UserPrefs.save(requireContext(), UserPrefs.Profile())
//                        DashboardConfig.clear(requireContext())
//                        setupAccountSection()
//                        toast("Local data cleared")
//                    }
//                    .setNegativeButton("Cancel", null)
//                    .show()
//            }
//        }
//
//        binding.btnLogout.setOnClickListener { view ->
//            animatePress(view) {
//                AlertDialog.Builder(requireContext())
//                    .setTitle("Sign Out?")
//                    .setMessage("You will need to sign in again.")
//                    .setPositiveButton("Sign Out") { _, _ ->
//                        UserPrefs.logout(requireContext())
//                        DashboardConfig.clear(requireContext())
//                        // FIX: requireNotNull to satisfy Intent? → Intent type
//                        val intent = requireNotNull(
//                            requireContext().packageManager
//                                .getLaunchIntentForPackage(requireContext().packageName)
//                        ).apply {
//                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                        }
//                        startActivity(intent)
//                    }
//                    .setNegativeButton("Cancel", null)
//                    .show()
//            }
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    //  RESTART APP (after dashboard switch)
//    // ─────────────────────────────────────────────────────────────
//
//    private fun restartApp() {
//        val intent = requireNotNull(
//            requireContext().packageManager
//                .getLaunchIntentForPackage(requireContext().packageName)
//        ).apply {
//            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//        }
//        startActivity(intent)
//        Runtime.getRuntime().exit(0)
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    //  HELPERS
//    // ─────────────────────────────────────────────────────────────
//
//    private fun animatePress(view: View, action: () -> Unit) {
//        view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction {
//            view.animate().scaleX(1f).scaleY(1f)
//                .setDuration(150).setInterpolator(DecelerateInterpolator()).start()
//            action()
//        }.start()
//    }
//
//    private fun toast(msg: String) =
//        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
//}


package com.project.integrationsdk.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.R
import com.project.integrationsdk.data.DashboardConfig
import com.project.integrationsdk.data.UserPrefs
import com.project.integrationsdk.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val ct by lazy { CleverTapAPI.getDefaultInstance(requireContext() as Context) }
    private var pendingDashboard: DashboardConfig.Dashboard? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentSettingsBinding.inflate(inflater, container, false)
        .also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        applyStatusBarInset()
        setupDashboardSection()
        setupMessagingToggles()
        setupAccountSection()
        setupAppInfo()
        setupDangerZone()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    // ─────────────────────────────────────────────────────────────
    //  STATUS BAR INSET
    // ─────────────────────────────────────────────────────────────

    private fun applyStatusBarInset() {
        val extra = (14 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarContainer) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, top + extra, v.paddingRight, v.paddingBottom)
            insets
        }
        ViewCompat.requestApplyInsets(binding.toolbarContainer)
    }

    // ─────────────────────────────────────────────────────────────
    //  SECTION 1: DASHBOARD SWITCHER
    // ─────────────────────────────────────────────────────────────

    private fun setupDashboardSection() {
        val dashboards = DashboardConfig.dashboards
        val active     = DashboardConfig.getActive(requireContext())

        // Set initial selected dashboard in the styled trigger
        pendingDashboard = active
        binding.tvSpinnerSelected.text = active.name
        updateDashboardDisplay(active)

        // Tap the styled trigger → show a PopupMenu (fully theme-aware, no black box)
        binding.spinnerDashboard.setOnClickListener { anchor ->
            val popup = PopupMenu(requireContext(), anchor)
            dashboards.forEachIndexed { index, dashboard ->
                popup.menu.add(0, index, index, dashboard.name)
            }
            popup.setOnMenuItemClickListener { item ->
                val selected = dashboards[item.itemId]
                pendingDashboard = selected
                binding.tvSpinnerSelected.text = selected.name
                updateDashboardDisplay(selected)
                true
            }
            popup.show()
        }

        binding.btnSwitchDashboard.setOnClickListener { view ->
            animatePress(view) {
                val selected = pendingDashboard ?: DashboardConfig.getActive(requireContext())
                val current  = DashboardConfig.getActive(requireContext())
                if (selected.id == current.id) {
                    toast("Already on ${selected.name}")
                    return@animatePress
                }
                AlertDialog.Builder(requireContext())
                    .setTitle("Switch to ${selected.name}?")
                    .setMessage("The app will restart and begin a new CleverTap session.\nAccount ID: ${selected.accountId}")
                    .setPositiveButton("Switch & Restart") { _, _ ->
                        DashboardConfig.setActive(requireContext(), selected)
                        restartApp()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun updateDashboardDisplay(d: DashboardConfig.Dashboard) {
        binding.tvAccountId.text = d.accountId
        binding.tvToken.text = if (d.token.length > 4)
            "•".repeat(d.token.length - 4) + d.token.takeLast(4)
        else d.token
        // Show region if present, hide row if null
        if (d.region != null) {
            binding.tvRegion.text    = d.region.uppercase()
            binding.rowRegion.visibility = android.view.View.VISIBLE
        } else {
            binding.rowRegion.visibility = android.view.View.GONE
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  SECTION 2: MESSAGING PREFERENCES
    //  FIX: Direct view references from binding — no ItemToggleRowBinding.
    //  FIX: HashMap cast issue — use mapOf which returns Map<String, Any>.
    // ─────────────────────────────────────────────────────────────

    private fun setupMessagingToggles() {
        val profile = UserPrefs.load(requireContext())

        // Apply icon tint + background colors programmatically
        applyIconStyle(binding.iconBgEmail, binding.iconEmail, "#EDE9FD", "#5B4FCF")
        applyIconStyle(binding.iconBgPush,  binding.iconPush,  "#FFE8E3", "#D9634F")
        applyIconStyle(binding.iconBgSms,   binding.iconSms,   "#E2FAF4", "#00A878")
        applyIconStyle(binding.iconBgWa,    binding.iconWa,    "#E2FAF4", "#00A878")

        // Set initial states
        binding.switchEmail.isChecked    = profile.msgEmail
        binding.switchPush.isChecked     = profile.msgPush
        binding.switchSms.isChecked      = profile.msgSms
        binding.switchWhatsapp.isChecked = profile.msgWhatsapp

        // FIX: use mapOf<String, Any> — no explicit cast needed
        binding.switchEmail.setOnCheckedChangeListener { _, checked ->
            ct?.pushProfile(mapOf("MSG-email" to checked as Any))
            UserPrefs.save(requireContext(), UserPrefs.load(requireContext()).copy(msgEmail = checked))
        }
        binding.switchPush.setOnCheckedChangeListener { _, checked ->
            ct?.pushProfile(mapOf("MSG-push" to checked as Any))
            UserPrefs.save(requireContext(), UserPrefs.load(requireContext()).copy(msgPush = checked))
        }
        binding.switchSms.setOnCheckedChangeListener { _, checked ->
            ct?.pushProfile(mapOf("MSG-sms" to checked as Any))
            UserPrefs.save(requireContext(), UserPrefs.load(requireContext()).copy(msgSms = checked))
        }
        binding.switchWhatsapp.setOnCheckedChangeListener { _, checked ->
            ct?.pushProfile(mapOf("MSG-whatsapp" to checked as Any))
            UserPrefs.save(requireContext(), UserPrefs.load(requireContext()).copy(msgWhatsapp = checked))
        }
    }

    private fun applyIconStyle(container: View, icon: android.widget.ImageView, bgHex: String, tintHex: String) {
        // Rounded square background
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10 * resources.displayMetrics.density
            setColor(Color.parseColor(bgHex))
        }
        container.background = bg
        icon.setColorFilter(Color.parseColor(tintHex))
    }

    // ─────────────────────────────────────────────────────────────
    //  SECTION 3: ACCOUNT
    // ─────────────────────────────────────────────────────────────

    private fun setupAccountSection() {
        val profile = UserPrefs.load(requireContext())
        binding.tvSignedInEmail.text = profile.email.ifEmpty { "—" }
        binding.tvIdentity.text      = profile.identity.ifEmpty { "—" }
    }

    // ─────────────────────────────────────────────────────────────
    //  SECTION 4: APP INFO
    //  FIX: CleverTapAPI.SDKVersion doesn't exist — use the correct
    //       constant com.clevertap.android.sdk.BuildConfig.SDK_VERSION_STRING
    //       or just read it from the SDK's gradle dependency string.
    // ─────────────────────────────────────────────────────────────

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupAppInfo() {
        // App version
        binding.tvAppVersion.text = try {
            val pi = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0)
            "${pi.versionName} (${pi.longVersionCode})"
        } catch (e: Exception) { "—" }

        // CleverTap Device ID — the __gq... value shown in CT dashboard
        // This is the unique identifier CleverTap assigns to this device.
        val deviceId = ct?.cleverTapID ?: "—"
        binding.tvDeviceId.text = deviceId

        // Tap to copy
        binding.tvDeviceId.setOnClickListener {
            val clipboard = requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(
                android.content.ClipData.newPlainText("CleverTap Device ID", deviceId)
            )
            toast("Device ID copied")
        }

        // Copy icon button
        binding.btnCopyDeviceId.setOnClickListener {
            val clipboard = requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(
                android.content.ClipData.newPlainText("CleverTap Device ID", deviceId)
            )
            toast("Device ID copied")
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  SECTION 5: DANGER ZONE
    // ─────────────────────────────────────────────────────────────

    private fun setupDangerZone() {
        binding.btnClearData.setOnClickListener { view ->
            animatePress(view) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Clear Local Data?")
                    .setMessage("Clears all cached profile data and dashboard selection. You will NOT be logged out.")
                    .setPositiveButton("Clear") { _, _ ->
                        UserPrefs.save(requireContext(), UserPrefs.Profile())
                        DashboardConfig.clear(requireContext())
                        setupAccountSection()
                        toast("Local data cleared")
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        binding.btnLogout.setOnClickListener { view ->
            animatePress(view) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Sign Out?")
                    .setMessage("You will need to sign in again.")
                    .setPositiveButton("Sign Out") { _, _ ->
                        UserPrefs.logout(requireContext())
                        DashboardConfig.clear(requireContext())
                        // FIX: requireNotNull to satisfy Intent? → Intent type
                        val intent = requireNotNull(
                            requireContext().packageManager
                                .getLaunchIntentForPackage(requireContext().packageName)
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  RESTART APP (after dashboard switch)
    // ─────────────────────────────────────────────────────────────

    private fun restartApp() {
        val intent = requireNotNull(
            requireContext().packageManager
                .getLaunchIntentForPackage(requireContext().packageName)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────

    private fun animatePress(view: View, action: () -> Unit) {
        view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction {
            view.animate().scaleX(1f).scaleY(1f)
                .setDuration(150).setInterpolator(DecelerateInterpolator()).start()
            action()
        }.start()
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}