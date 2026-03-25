package com.project.integrationsdk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.ActivityUploadEventsBinding

class UploadEventsActivity : AppCompatActivity() {

    lateinit var binding: ActivityUploadEventsBinding
    private val ctInstance by lazy {
        CleverTapAPI.getDefaultInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdd.setOnClickListener {
            addKeyValueRow()
        }

        binding.btnSubmit.setOnClickListener {
            raiseCustomEvent()
        }

    }
    private fun addKeyValueRow() {
        val row = LayoutInflater.from(this).inflate(R.layout.item_key_value, binding.keyValueContainer, false)
        binding.keyValueContainer.addView(row)
    }
    private fun raiseCustomEvent() {
        val eventName = binding.etEventName.text.toString().trim()
        if (eventName.isEmpty()) return

        val eventProps = HashMap<String, Any>()

        for (i in 0 until binding.keyValueContainer.childCount) {
            val row = binding.keyValueContainer.getChildAt(i)
            val etKey = row.findViewById<EditText>(R.id.etKey)
            val etValue = row.findViewById<EditText>(R.id.etValue)
            val key = etKey.text.toString().trim()
            val value = etValue.text.toString().trim()

            if (key.isNotEmpty() && value.isNotEmpty()) {
                eventProps[key] = parseValue(value)
            }
        }
        ctInstance?.pushEvent(eventName, eventProps)
        Toast.makeText(applicationContext, "${eventName} raised", Toast.LENGTH_SHORT).show()
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