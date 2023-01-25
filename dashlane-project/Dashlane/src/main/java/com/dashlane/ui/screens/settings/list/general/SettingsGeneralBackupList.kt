package com.dashlane.ui.screens.settings.list.general

import android.content.Context
import com.dashlane.R
import com.dashlane.securearchive.BackupCoordinator
import com.dashlane.ui.screens.settings.item.SensibleSettingsClickHelper
import com.dashlane.ui.screens.settings.item.SettingHeader
import com.dashlane.ui.screens.settings.item.SettingItem



class SettingsGeneralBackupList(
    context: Context,
    backupCoordinator: BackupCoordinator,
    sensibleSettingsClickHelper: SensibleSettingsClickHelper
) {
    private val backupHeader =
        SettingHeader(context.getString(R.string.setting_backup_category))

    private val backupExportItem = object : SettingItem {
        override val id = "backup-export"
        override val header = backupHeader
        override val title = context.getString(R.string.setting_backup_export)
        override val description = context.getString(R.string.setting_backup_export_description)
        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) = true

        override fun onClick(context: Context) = sensibleSettingsClickHelper.perform(
            context = context,
            forceMasterPassword = true
        ) {
            backupCoordinator.startExport()
        }
    }

    private val backupImportItem = object : SettingItem {
        override val id = "backup-import"
        override val header = backupHeader
        override val title = context.getString(R.string.setting_backup_import)
        override val description = context.getString(R.string.setting_backup_import_description)
        override fun isEnable(context: Context) = true
        override fun isVisible(context: Context) =
            backupExportItem.isVisible(context)

        override fun onClick(context: Context) = backupCoordinator.startImport()
    }

    fun getAll() = listOf(
        backupExportItem,
        backupImportItem
    )
}