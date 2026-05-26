package com.project.integrationsdk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.R
import com.project.integrationsdk.data.CleverTapManager
import com.project.integrationsdk.databinding.ActivityUploadUserPropertiesBinding

class UploadUserPropertiesActivity : AppCompatActivity() {

    lateinit var binding: ActivityUploadUserPropertiesBinding
    private val ctInstance by lazy {
        CleverTapManager.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadUserPropertiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdd.setOnClickListener {
            addKeyValueRow()
        }

        binding.btnSubmit.setOnClickListener {
            raiseUserProperty()
        }
    }

    private fun addKeyValueRow() {
        val row = LayoutInflater.from(this).inflate(R.layout.item_key_value, binding.keyValueContainer, false)
        binding.keyValueContainer.addView(row)
    }

    private fun raiseUserProperty() {
        val userProps = HashMap<String, Any>()
        val mainKey = binding.etMainKey.text.toString().trim()
        val mainValue = binding.etMainValue.text.toString().trim()
        if (mainKey.isNotEmpty() && mainValue.isNotEmpty()) {
            userProps[mainKey] = parseValue(mainValue)
        }
        for (i in 0 until binding.keyValueContainer.childCount) {
            val row = binding.keyValueContainer.getChildAt(i)
            val etKey = row.findViewById<EditText>(R.id.etKey)
            val etValue = row.findViewById<EditText>(R.id.etValue)
            val key = etKey.text.toString().trim()
            val value = etValue.text.toString().trim()
            if (key.isNotEmpty() && value.isNotEmpty()) {
                userProps[key] = parseValue(value)
            }
        }
        if (userProps.isEmpty()) {
            Toast.makeText(this, "Please enter at least one property", Toast.LENGTH_SHORT).show()
            return
        }
        ctInstance?.pushProfile(userProps)
        Toast.makeText(this, "User property uploaded", Toast.LENGTH_SHORT).show()
    }

    private fun parseValue(value: String): Any {

        return when {
            value.equals("true", ignoreCase = true) -> true
            value.equals("false", ignoreCase = true) -> false

            value.toIntOrNull() != null -> value.toInt()
            value.toLongOrNull() != null -> value.toLong()
            value.toFloatOrNull() != null -> value.toFloat()
            value.toDoubleOrNull() != null -> value.toDouble()

            else -> value
        }
    }
}
