package com.dashlane.welcome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import androidx.viewpager.widget.ViewPager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.component.EndOfLifeComponent
import com.dashlane.ui.endoflife.EndOfLife
import com.dashlane.util.clearTop
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.logE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeActivity : DashlaneActivity() {

    private val endOfLife: EndOfLife
        get() = EndOfLifeComponent(this).endOfLife

    override var requireUserUnlock = false

    @Inject
    lateinit var welcomeLogger: WelcomeLogger

    @Inject
    lateinit var hasOtpsForBackupProvider: HasOtpsForBackupProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val loginIntent = intent.getParcelableExtraCompat<Intent>(EXTRA_LOGIN_INTENT)
        val createAccountIntent = intent.getParcelableExtraCompat<Intent>(EXTRA_CREATE_ACCOUNT_INTENT)
        if (loginIntent == null || createAccountIntent == null) {
            logE { "Cannot start without loginIntent or createAccountIntent" }
            finish()
            return
        }

        setupView(loginIntent, createAccountIntent)

        if (savedInstanceState == null) {
            welcomeLogger.logDisplayed()
            welcomeLogger.logPageDisplayed(0)
        }

        launch(Dispatchers.Main) {
            endOfLife.checkBeforeSession(this@WelcomeActivity)
        }
    }

    private fun setupView(loginIntent: Intent, createAccountIntent: Intent) {
        findViewById<ViewPager>(R.id.view_pager).run {
            runCatching {
                
                val scroller = ViewPager::class.java.getDeclaredField("mScroller")
                scroller.isAccessible = true
                val scrollDuration = context.resources.getInteger(android.R.integer.config_mediumAnimTime)
                scroller.set(this@run, object : Scroller(context, DecelerateInterpolator()) {
                    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
                        super.startScroll(startX, startY, dx, dy, scrollDuration)
                    }

                    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
                        super.startScroll(startX, startY, dx, dy, scrollDuration)
                    }
                })
            }

            val welcomeAdapter = WelcomePagerAdapter(
                hasOtpsForBackupProvider
            )
            adapter = welcomeAdapter
            addOnPageChangeListener(welcomeAdapter)
            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    welcomeLogger.logPageDisplayed(position)
                }
            })
        }

        findViewById<View>(R.id.button_login).setOnClickListener {
            welcomeLogger.logLoginClicked()
            startActivity(loginIntent.clearTop())
        }

        findViewById<View>(R.id.button_create_account).setOnClickListener {
            welcomeLogger.logGetStartedClicked()
            startActivity(createAccountIntent.clearTop())
        }
    }

    companion object {
        private const val EXTRA_LOGIN_INTENT = "login_intent"
        private const val EXTRA_CREATE_ACCOUNT_INTENT = "create_account_intent"

        fun newIntent(
            context: Context,
            loginIntent: Intent,
            createAccountIntent: Intent
        ): Intent = Intent(context, WelcomeActivity::class.java)
            .putExtra(EXTRA_LOGIN_INTENT, loginIntent)
            .putExtra(EXTRA_CREATE_ACCOUNT_INTENT, createAccountIntent)
    }
}