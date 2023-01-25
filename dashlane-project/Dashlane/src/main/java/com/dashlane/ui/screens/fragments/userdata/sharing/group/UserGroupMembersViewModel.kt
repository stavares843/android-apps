package com.dashlane.ui.screens.fragments.userdata.sharing.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.core.DataSync
import com.dashlane.events.AppEvents
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.session.SessionManager
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingUserGroupUser
import com.dashlane.useractivity.log.usage.UsageLogCode134
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserGroupMembersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataProvider: UserGroupDataProvider,
    appEvents: AppEvents,
    private val sessionManager: SessionManager
) : ViewModel(), UserGroupMembersViewModelContract {

    private val userId: String?
        get() = sessionManager.session?.userId

    override val userGroupId = savedStateHandle.get<String>(UserGroupMembersFragment.ARGS_GROUP_ID)!!

    override val uiState: MutableStateFlow<UserGroupMembersViewModelContract.UIState> =
        MutableStateFlow(UserGroupMembersViewModelContract.UIState.Loading)

    private val dataList = mutableListOf<SharingUserGroupUser>()

    init {
        reloadData()
        appEvents.register(this, SyncFinishedEvent::class.java, false) {
            reloadData()
        }
    }

    override fun reloadData() {
        val userId = userId ?: return
        viewModelScope.launch {
            runCatching {
                dataProvider.getMembersForUserGroup(userGroupId, userId)
            }.onSuccess {
                dataList.apply {
                    clear()
                    addAll(it)
                    if (isEmpty()) {
                        uiState.tryEmit(UserGroupMembersViewModelContract.UIState.Empty)
                    } else {
                        uiState.tryEmit(UserGroupMembersViewModelContract.UIState.Data(it))
                    }
                }
            }.onFailure {
                uiState.tryEmit(UserGroupMembersViewModelContract.UIState.Empty)
            }
        }
    }

    override fun pullToRefresh() = DataSync.sync(UsageLogCode134.Origin.MANUAL)
}