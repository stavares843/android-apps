package com.dashlane.autofill.api.viewallaccounts.services

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.vault.summary.SummaryObject



interface ViewAllAccountSelectionNotifier {
    fun onAccountSelected(formSource: AutoFillFormSource, account: SummaryObject.Authentifiant)
}
