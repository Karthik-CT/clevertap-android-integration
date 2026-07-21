package com.project.integrationsdk.util

import android.util.Log
import android.view.View
import android.widget.TextView
import com.clevertap.android.sdk.CleverTapAPI

private const val TAG = "InboxTtlSweep"

/**
 * Reconciles TTL-expired App Inbox messages.
 *
 * Every unread message carries a `wzrk_ttl` in its raw server payload — a Unix
 * timestamp in SECONDS marking when the message should expire. When the user
 * opens / returns to the app, any unread message whose TTL has been reached or
 * crossed (now >= wzrk_ttl) is stale, so we mark it as read via
 * markReadInboxMessage(). That decrements the inbox unread count and keeps the
 * notification-bell badge in sync.
 *
 * Why "on resume" rather than an exact-moment timer: the badge only exists
 * while the UI is visible. If the TTL passes while the app is killed/backgrounded
 * there is no badge to update — we simply reconcile the next time a screen
 * becomes visible (this is called from every badge render + inbox callbacks),
 * which is the only moment the count is actually looked at.
 *
 * @return the number of messages marked as read.
 */
fun CleverTapAPI?.markExpiredInboxMessagesAsRead(): Int {
    val cleverTap = this ?: return 0
    val nowSeconds = System.currentTimeMillis() / 1000
    var marked = 0
    cleverTap.unreadInboxMessages?.forEach { msg ->
        val wzrkTtl = msg.data.optLong("wzrk_ttl", -1)
        if (wzrkTtl != -1L && nowSeconds >= wzrkTtl) {
            cleverTap.markReadInboxMessage(msg.messageId)
            marked++
            Log.d(TAG, "Marked read (TTL reached): id=${msg.messageId} wzrk_ttl=$wzrkTtl now=$nowSeconds")
        }
    }
    if (marked > 0) Log.d(TAG, "Marked $marked expired message(s) as read")
    return marked
}

/**
 * Reads the current App Inbox unread count from the given CleverTap instance
 * (cleverTap?.inboxMessageUnreadCount) and renders it on the notification-bell
 * badge TextView. The badge is hidden when there are no unread messages and
 * caps the displayed value at "99+".
 *
 * Before reading the count, any unread message whose `wzrk_ttl` has been
 * reached/crossed is marked as read so the badge never counts stale messages.
 *
 * Note: the unread count is only meaningful once the inbox has been
 * initialized (initializeInbox()), so call this from inboxDidInitialize() /
 * inboxMessagesDidUpdate() and onResume().
 */
fun TextView.bindInboxUnreadCount(cleverTap: CleverTapAPI?) {
    cleverTap.markExpiredInboxMessagesAsRead()
    val count = cleverTap?.inboxMessageUnreadCount ?: 0
    if (count > 0) {
        text = if (count > 99) "99+" else count.toString()
        visibility = View.VISIBLE
    } else {
        visibility = View.GONE
    }
}
