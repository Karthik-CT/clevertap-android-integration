package com.project.integrationsdk.util

import android.view.View
import android.widget.TextView
import com.clevertap.android.sdk.CleverTapAPI

/**
 * Reads the current App Inbox unread count from the given CleverTap instance
 * (cleverTap?.inboxMessageUnreadCount) and renders it on the notification-bell
 * badge TextView. The badge is hidden when there are no unread messages and
 * caps the displayed value at "99+".
 *
 * Note: the unread count is only meaningful once the inbox has been
 * initialized (initializeInbox()), so call this from inboxDidInitialize() /
 * inboxMessagesDidUpdate() and onResume().
 */
fun TextView.bindInboxUnreadCount(cleverTap: CleverTapAPI?) {
    val count = cleverTap?.inboxMessageUnreadCount ?: 0
    if (count > 0) {
        text = if (count > 99) "99+" else count.toString()
        visibility = View.VISIBLE
    } else {
        visibility = View.GONE
    }
}
