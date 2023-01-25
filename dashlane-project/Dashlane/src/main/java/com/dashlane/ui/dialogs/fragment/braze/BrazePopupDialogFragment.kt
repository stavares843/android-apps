package com.dashlane.ui.dialogs.fragment.braze

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.braze.R
import com.dashlane.design.component.compat.view.ButtonMediumView
import com.dashlane.url.toUrlDomain
import com.dashlane.url.toUrlDomainOrNull



class BrazePopupDialogFragment : NotificationDialogFragment() {
    private val announcementCenter = SingletonProvider.getAnnouncementCenter()
    private val navigator = SingletonProvider.getNavigator()

    private val messageId: String
        get() = requireArguments().getString(ARG_ID, "")

    override fun onCreateDialogCustomView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflater ?: return null
        val view = inflater.inflate(R.layout.fragment_in_app_message, container, false)
        view.apply {
            arguments?.getString(ARG_BANNER_URL)?.let { bannerUrl ->
                findViewById<ImageView>(R.id.dialog_banner)?.let { imageView ->
                    Glide.with(imageView.context)
                        .load(bannerUrl)
                        .fitCenter()
                        .into(imageView)
                }
            }
            findViewById<TextView>(R.id.dialog_title)?.text = arguments?.getString(ARG_TITLE)
            findViewById<TextView>(R.id.dialog_text)?.text = arguments?.getString(ARG_MESSAGE)

            findViewById<ButtonMediumView>(R.id.dialog_positive_button)?.apply {
                text = arguments?.getString(ARG_POSITIVE_BUTTON_TEXT)
                onClick = {
                    navigateSecurely(arguments?.getString(ARG_POSITIVE_BUTTON_URI))
                    onClickPositiveButton()
                    dismiss()
                }
            }

            arguments?.getString(ARG_NEGATIVE_BUTTON_TEXT)?.let {
                findViewById<ButtonMediumView>(R.id.dialog_negative_button)?.apply {
                    text = it
                    onClick = {
                        navigateSecurely(arguments?.getString(ARG_NEGATIVE_BUTTON_URI))
                        onClickNegativeButton()
                        dismiss()
                    }
                    isEnabled = true
                    visibility = View.VISIBLE
                }
            }
        }
        return view
    }

    override fun onCancel(dialog: DialogInterface) {
        announcementCenter.announcementClosed(messageId)
        super.onCancel(dialog)
    }

    

    private fun navigateSecurely(uri: String?) {
        uri ?: return
        val isDashlaneDomain = uri.toUrlDomainOrNull()?.root == "dashlane.com".toUrlDomain()
        val isDeeplink = uri.startsWith("dashlane:///")
        when {
            isDeeplink -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = uri.toUri()
                navigator.handleDeepLink(intent)
            }

            isDashlaneDomain -> {
                navigator.goToWebView(uri)
            }
        }
    }

    companion object {
        const val ARG_BANNER_URL = "arg_banner_url"
        const val ARG_TITLE = "arg_title"
        const val ARG_MESSAGE = "arg_message"

        const val ARG_POSITIVE_BUTTON_TEXT = "arg_positive_button_text"
        const val ARG_POSITIVE_BUTTON_URI = "arg_positive_button_uri"

        const val ARG_NEGATIVE_BUTTON_TEXT = "arg_negative_button_text"
        const val ARG_NEGATIVE_BUTTON_URI = "arg_negative_button_uri"

        const val ARG_ID = "_arg_message_id"
        const val ARG_TRACKING_KEY = "_arg_tracking_key"
    }
}