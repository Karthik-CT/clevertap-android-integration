package com.project.integrationsdk

import android.app.Activity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI

open class BaseActivity : AppCompatActivity() {

    private val ctInstance by lazy {
        CleverTapAPI.getDefaultInstance(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_refresh)?.icon?.setTint(
            getColor(R.color.white)
        )
        menu?.findItem(R.id.action_notification)?.icon?.setTint(
            getColor(R.color.white)
        )
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.action_refresh -> {
                restartApp(this)
                Toast.makeText(this, "Refresh clicked", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.action_notification -> {
                ctInstance?.showAppInbox()
                Toast.makeText(this, "App Inbox clicked", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun restartApp(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }
}