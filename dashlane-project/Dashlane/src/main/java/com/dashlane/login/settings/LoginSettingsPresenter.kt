package com.dashlane.login.settings

import com.dashlane.accountrecovery.AccountRecovery
import com.dashlane.hermes.LogRepository
import com.dashlane.login.LoginSuccessIntentFactory
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.session.Session
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.LockRepository
import com.dashlane.ui.screens.settings.UserSettingsLogRepository
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.skocken.presentation.definition.Base
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginSettingsPresenter @Inject constructor(
    @GlobalCoroutineScope
    private val globalCoroutineScope: CoroutineScope,
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    val logger: LoginSettingsContract.Logger,
    private val loginSuccessIntentFactory: LoginSuccessIntentFactory,
    private val sessionManager: SessionManager,
    private val accountRecovery: AccountRecovery,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val lockRepository: LockRepository,
    private val biometricAuthModule: BiometricAuthModule,
    private val logRepository: LogRepository,
    private val userSettingsLogRepository: UserSettingsLogRepository
) : BasePresenter<Base.IDataProvider, LoginSettingsContract.ViewProxy>(),
    LoginSettingsContract.Presenter {

    override fun onViewChanged() {
        super.onViewChanged()
        if (!biometricAuthModule.isHardwareSetUp()) {
            goToLoginSyncProgress()
        } else {
            logger.logDisplay()
        }
    }

    override fun onNext() {
        logger.logNext()
        sessionManager.session?.let { session ->
            if (view.biometricSettingChecked) {
                globalCoroutineScope.launch(defaultCoroutineDispatcher) {
                    enableBiometric(session)
                    if (view.resetMpSettingChecked) {
                        enableResetMp()
                    }
                    logRepository.queueEvent(userSettingsLogRepository.get())
                }
            }
        }

        goToLoginSyncProgress()
    }

    private fun goToLoginSyncProgress() {
        val intent = loginSuccessIntentFactory.createLoginSyncProgressIntent()
        view.context.startActivity(intent)
        activity?.finishAffinity()
    }

    private suspend fun enableBiometric(session: Session) = withContext(Dispatchers.Default) {
        
        runCatching {
            sessionCredentialsSaver.saveCredentials(session)
            
            val result = biometricAuthModule.createEncryptionKeyForBiometrics(username = session.userId)
            if (!result) return@withContext
        }
        
        lockRepository.getLockManager(session).setLockType(LockTypeManager.LOCK_TYPE_BIOMETRIC)
    }

    private fun enableResetMp() {
        
        accountRecovery.isFeatureKnown = true
        accountRecovery.setFeatureEnabled(true, "firstLogin")
    }
}
