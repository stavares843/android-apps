package com.dashlane.premium.paywall.darkwebmonitoring

import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.premium.R
import com.dashlane.premium.paywall.common.PaywallIntro

class DarkWebMonitoringIntro(private val logger: DarkWebMonitoringLegacyLogger) : PaywallIntro {
    override val image = R.drawable.ic_paywall_dwm
    override val title = R.string.paywall_intro_dwm_title
    override val message = R.string.paywall_intro_dwm_message
    override val messageFormatArgs: Array<Any> = emptyArray()
    override val trackingKey = "dark_web"
    override val page = AnyPage.PAYWALL_DARK_WEB_MONITORING

    override fun onShowPaywall() {
        logger.onShowPremiumPrompt()
    }

    override fun onClickSeeAllOptions() {
        logger.onClickPremiumPromptGoPremium()
    }

    override fun onClickUpgrade() {
        logger.onClickPremiumPromptGoPremium()
    }

    override fun onClickClose() {
        logger.getClickPremiumPromptClose()
    }
}