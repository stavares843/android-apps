package com.dashlane.ui.activities.fragments.vault.provider

import android.content.Context
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter

object SuggestedHeaderProvider : HeaderProvider {
    override fun getHeaderFor(context: Context, viewTypeProvider: DashlaneRecyclerAdapter.ViewTypeProvider) =
        context.getString(R.string.vault_header_suggested)
}