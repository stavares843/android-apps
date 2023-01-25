package com.dashlane.item.subview.action

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.dashlane.R
import com.dashlane.attachment.ui.AttachmentListActivity
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.NavigationUriBuilder
import com.dashlane.ui.activities.DashlaneWrapperActivity
import com.dashlane.useractivity.log.usage.UsageLogCode80
import com.dashlane.util.getThemeAttrColor
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.desktopId

fun VaultItem<*>.showAttachments(activity: Activity, color: Int) {
    this.toSummary<SummaryObject>().showAttachments(activity, color)
}

fun SummaryObject.showAttachments(activity: Activity, color: Int) {
    var activityColor = color
    val attachmentListIntent = Intent(activity, AttachmentListActivity::class.java).apply {
        putExtra(AttachmentListActivity.ITEM_ATTACHMENTS, attachments)
        if (activityColor == -1) {
            
            activityColor = activity.getThemeAttrColor(R.attr.colorPrimary)
        }
        putExtra(AttachmentListActivity.ITEM_COLOR, activityColor)
        putExtra(AttachmentListActivity.ITEM_ID, id)
        putExtra(
            AttachmentListActivity.ITEM_TYPE,
            this@showAttachments.syncObjectType.desktopId
        )
    }
    activity.startActivityForResult(
        attachmentListIntent,
        AttachmentListActivity.REQUEST_CODE_ATTACHMENT_LIST
    )
}

fun SummaryObject.showSharing(
    requestCode: Int,
    activity: Activity,
    startNewShare: Boolean = false
) {
    val uri = NavigationUriBuilder().apply {
        when (this@showSharing) {
            is SummaryObject.Authentifiant -> {
                host(NavigationHelper.Destination.MainPath.PASSWORDS)
                origin(UsageLogCode80.From.CREDENTIALS)
            }
            is SummaryObject.SecureNote -> {
                host(NavigationHelper.Destination.MainPath.NOTES)
                origin(UsageLogCode80.From.SECURE_NOTES)
            }
            else -> {
                
            }
        }
        appendPath(id)
        if (startNewShare) {
            appendPath(NavigationHelper.Destination.SecondaryPath.Items.SHARE)
        } else {
            appendPath(NavigationHelper.Destination.SecondaryPath.Items.SHARE_INFO)
        }
    }.build()
    DashlaneWrapperActivity.startActivityForResult(requestCode, activity, uri, Bundle().apply {
        if (!startNewShare) putBoolean(ShareDetailsAction.EXTRA_NOTIFY_UID_CHANGES, true)
    })
}

fun VaultItem<*>.showSharing(requestCode: Int, activity: Activity, startNewShare: Boolean = false) {
    toSummary<SummaryObject>().showSharing(requestCode, activity, startNewShare)
}