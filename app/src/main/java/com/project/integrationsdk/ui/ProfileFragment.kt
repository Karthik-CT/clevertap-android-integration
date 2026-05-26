package com.project.integrationsdk.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.data.CleverTapHelper
import com.project.integrationsdk.data.CleverTapManager
import com.project.integrationsdk.data.UserPrefs
import com.project.integrationsdk.databinding.FragmentProfileBinding
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val ct by lazy { CleverTapManager.getInstance(requireContext() as Context) }
    private var selectedDob: Date? = null
    private val displayFormat = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentProfileBinding.inflate(inflater, container, false)
        .also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        applyStatusBarInset()
        populateForm()
        setupDobPicker()
        setupSaveButtons()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    // ─────────────────────────────────────────────────────────────
    //  STATUS BAR INSET
    // ─────────────────────────────────────────────────────────────

    private fun applyStatusBarInset() {
        val extraPadding = (14 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarContainer) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, top + extraPadding, v.paddingRight, v.paddingBottom)
            insets
        }
        ViewCompat.requestApplyInsets(binding.toolbarContainer)
    }

    // ─────────────────────────────────────────────────────────────
    //  POPULATE FORM
    //  Reads from UserPrefs. Messaging prefs are now in Settings —
    //  only personal info fields are populated here.
    // ─────────────────────────────────────────────────────────────

    private fun populateForm() {
        val profile = UserPrefs.load(requireContext())

        binding.etFirstName.setText(profile.firstName)
        binding.etLastName.setText(profile.lastName)
        binding.etEmail.setText(profile.email)   // read-only
        binding.etPhone.setText(profile.phone)

        // DOB
        selectedDob = profile.dob
        profile.dob?.let { binding.etDOB.setText(displayFormat.format(it)) }

        // Gender
        when (profile.gender) {
            "M" -> binding.rbMale.isChecked = true
            "F" -> binding.rbFemale.isChecked = true
            "O" -> binding.rbOther.isChecked = true
        }

        refreshAvatar(profile)
    }

    // ─────────────────────────────────────────────────────────────
    //  DOB PICKER
    // ─────────────────────────────────────────────────────────────

    private fun setupDobPicker() {
        binding.etDOB.setOnClickListener {
            val cal = Calendar.getInstance()
            selectedDob?.let { d -> cal.time = d }
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    cal.set(year, month, day, 0, 0, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    selectedDob = cal.time
                    binding.etDOB.setText(displayFormat.format(selectedDob!!))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  SAVE
    // ─────────────────────────────────────────────────────────────

    private fun setupSaveButtons() {
        binding.btnSave.setOnClickListener { attemptSave(it) }
        binding.btnSaveBottom.setOnClickListener { attemptSave(it) }
    }

    private fun attemptSave(view: View) {
        view.animate().scaleX(0.94f).scaleY(0.94f).setDuration(80).withEndAction {
            view.animate().scaleX(1f).scaleY(1f)
                .setDuration(150).setInterpolator(DecelerateInterpolator()).start()
        }.start()

        if (!validate()) return

        val updatedProfile = buildProfileFromForm()
        CleverTapHelper.updateProfile(ct, updatedProfile)
        UserPrefs.save(requireContext(), updatedProfile)
        refreshAvatar(updatedProfile)
        toast("Profile updated successfully")
    }

    // ─────────────────────────────────────────────────────────────
    //  VALIDATION
    // ─────────────────────────────────────────────────────────────

    private fun validate(): Boolean {
        val firstName = binding.etFirstName.text?.toString()?.trim() ?: ""
        val lastName = binding.etLastName.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""

        if (firstName.isEmpty()) {
            binding.etFirstName.error = "First name is required"
            binding.etFirstName.requestFocus()
            return false
        }
        if (lastName.isEmpty()) {
            binding.etLastName.error = "Last name is required"
            binding.etLastName.requestFocus()
            return false
        }
        if (phone.isNotEmpty() && !phone.startsWith("+")) {
            binding.etPhone.error = "Must start with country code e.g. +91"
            binding.etPhone.requestFocus()
            return false
        }
        return true
    }

    // ─────────────────────────────────────────────────────────────
    //  BUILD PROFILE FROM FORM
    //  Email + Identity preserved from prefs (read-only in UI).
    //  Messaging prefs preserved from prefs (edited in Settings).
    // ─────────────────────────────────────────────────────────────

    private fun buildProfileFromForm(): UserPrefs.Profile {
        val existing = UserPrefs.load(requireContext())
        return UserPrefs.Profile(
            firstName = binding.etFirstName.text?.toString()?.trim() ?: "",
            lastName = binding.etLastName.text?.toString()?.trim() ?: "",
            email = existing.email,       // read-only — never overwrite
            phone = binding.etPhone.text?.toString()?.trim() ?: "",
            identity = existing.identity,    // identity key — never overwrite
            gender = when {
                binding.rbMale.isChecked -> "M"
                binding.rbFemale.isChecked -> "F"
                binding.rbOther.isChecked -> "O"
                else -> ""
            },
            dob = selectedDob,
            // ── Messaging prefs owned by Settings — preserve as-is ──
            msgEmail = existing.msgEmail,
            msgPush = existing.msgPush,
            msgSms = existing.msgSms,
            msgWhatsapp = existing.msgWhatsapp
        )
    }

    // ─────────────────────────────────────────────────────────────
    //  AVATAR
    // ─────────────────────────────────────────────────────────────

    private fun refreshAvatar(profile: UserPrefs.Profile) {
        binding.tvAvatar.text = profile.initials
        binding.tvAvatarName.text = profile.fullName.ifEmpty { "Your Name" }
        binding.tvAvatarEmail.text = profile.email.ifEmpty { "your@email.com" }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}