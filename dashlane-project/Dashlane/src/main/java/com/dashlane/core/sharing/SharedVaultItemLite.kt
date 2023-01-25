package com.dashlane.core.sharing

import android.os.Parcelable
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.url.root
import com.dashlane.url.toUrlOrNull
import com.dashlane.vault.model.DataIdentifierId
import com.dashlane.vault.model.navigationUrl
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.desktopId
import kotlinx.parcelize.Parcelize

@Parcelize
data class SharedVaultItemLite(
    val uid: String,
    val anonymousId: String,
    @DataIdentifierId.Def
    val desktopId: Int,
    val website: String?,
    val title: String,
    val itemType: ItemForEmailing.Type
) : Parcelable {
    constructor(summaryObject: SummaryObject) : this(
        summaryObject.id,
        summaryObject.anonymousId ?: "",
        summaryObject.syncObjectType.desktopId,
        summaryObject.let {
            if (it is SummaryObject.Authentifiant) {
                it.navigationUrl?.toUrlOrNull()?.root
            } else {
                null
            }
        },
        summaryObject.let {
            when (it) {
                is SummaryObject.Authentifiant -> it.title
                is SummaryObject.SecureNote -> it.title
                else -> null
            }
        } ?: "",
        summaryObject.let {
            when (it) {
                is SummaryObject.Authentifiant -> ItemForEmailing.Type.PASSWORD
                is SummaryObject.SecureNote -> ItemForEmailing.Type.NOTE
                else -> throw IllegalStateException("Not a shared type ${summaryObject.syncObjectType}")
            }
        }
    )
}

fun SummaryObject.toSharedVaultItemLite(): SharedVaultItemLite {
    return SharedVaultItemLite(this)
}

fun SharedVaultItemLite.toItemForEmailing(): ItemForEmailing = ItemForEmailing(title, itemType)