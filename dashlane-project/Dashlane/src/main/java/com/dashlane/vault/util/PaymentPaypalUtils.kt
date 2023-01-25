@file:JvmName("PaymentPaypalUtils")

package com.dashlane.vault.util

import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.formatTitle
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject

fun VaultItem<SyncObject.PaymentPaypal>.toAuthentifiant(): VaultItem<SyncObject.Authentifiant> {
    return createAuthentifiant(
        dataIdentifier = CommonDataIdentifierAttrsImpl(
            syncState = SyncState.MODIFIED,
            teamSpaceId = TeamSpaceUtils.getTeamSpaceId(this)
        ),
        title = SyncObject.Authentifiant.formatTitle(syncObject.name),
        deprecatedUrl = "www.paypal.com",
        email = syncObject.login,
        password = syncObject.password?.let { SyncObfuscatedValue(it) }
    )
}
