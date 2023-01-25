package com.dashlane.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.notification.creator.AutoFillNotificationCreator

class AutofillReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getBooleanExtra(NOTIFICATION_NOT_NOW_EXTRA, false)
        if (!action) return

        val preferenceManager = SingletonProvider.getComponent().globalPreferencesManager
        preferenceManager.incrementAutofillNotificationDismiss()

        if (preferenceManager.getAutofillNotificationDismissCount() >= AutoFillNotificationCreator.DISMISSAL_THRESHOLD) {
            AutoFillNotificationCreator.cancelAutofillNotificationWorkers(context)
        }

        clearNotification(context)
    }

    private fun clearNotification(context: Context?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        notificationManager.cancel(AutoFillNotificationCreator.NOTIFICATION_ID)
    }

    companion object {
        const val NOTIFICATION_NOT_NOW_EXTRA = "notification_not_now"
    }
}