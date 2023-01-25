package com.dashlane.ui.fab

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.ui.activities.fragments.vault.Filter
import com.dashlane.util.getBaseActivity
import com.skocken.presentation.presenter.BasePresenter

class FabPresenter : BasePresenter<FabDef.IDataProvider?, FabDef.IView?>(), FabDef.IPresenter {
    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val handleClick = onBackPressedCaught()
            if (!handleClick) {
                SingletonProvider.getNavigator().popBackStack()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getView().hideFABMenu(false)
        (context!!.getBaseActivity() as AppCompatActivity).onBackPressedDispatcher
            .addCallback(backCallback)
    }

    override fun onBackPressedCaught(): Boolean {
        if (view.isFabMenuHolderVisible) {
            view.hideFABMenu(true)
            return true
        }
        return false
    }

    override fun onDestroyView() {
        backCallback.remove()
    }

    override fun setFilter(filter: Filter) {
        viewOrNull?.setFilter(filter)
    }
}
