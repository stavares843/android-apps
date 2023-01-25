package com.dashlane.item.subview.quickaction

import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.item.subview.action.CopyAction
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemFieldContentService
import com.dashlane.vault.summary.SummaryObject

class QuickActionCopy(
    summaryObject: SummaryObject,
    itemListContext: ItemListContext,
    val copyField: CopyField
) : CopyAction(summaryObject, copyField, itemListContext = itemListContext) {
    override val tintColorRes = R.color.text_neutral_catchy

    override val icon: Int = R.drawable.ic_item_action_copy

    override val text: Int
        get() {
            return when (copyField) {
                CopyField.Password, CopyField.PayPalPassword -> R.string.quick_action_copy_password
                CopyField.Login, CopyField.PayPalLogin, CopyField.IdentityLogin -> R.string.quick_action_copy_login
                CopyField.Email -> R.string.quick_action_copy_email
                CopyField.SecondaryLogin -> R.string.quick_action_copy_secondary_login
                CopyField.PaymentsNumber,
                CopyField.TaxNumber,
                CopyField.SocialSecurityNumber,
                CopyField.PassportNumber,
                CopyField.DriverLicenseNumber -> R.string.quick_action_copy_number
                CopyField.PaymentsSecurityCode -> R.string.quick_action_copy_credit_card_security_code
                CopyField.PaymentsExpirationDate,
                CopyField.IdsExpirationDate,
                CopyField.DriverLicenseExpirationDate,
                CopyField.PassportExpirationDate -> R.string.quick_action_copy_expiration_date
                CopyField.BankAccountBank -> R.string.quick_action_copy_bank_name
                CopyField.BankAccountBicSwift -> R.string.quick_action_copy_swift
                CopyField.BankAccountIban -> R.string.quick_action_copy_iban
                CopyField.Address -> R.string.quick_action_copy_address
                CopyField.City -> R.string.quick_action_copy_city
                CopyField.ZipCode -> R.string.quick_action_copy_zip_code
                CopyField.IdsNumber -> R.string.quick_action_copy_number
                CopyField.IdsIssueDate,
                CopyField.PassportIssueDate,
                CopyField.DriverLicenseIssueDate -> R.string.quick_action_copy_issue_date
                CopyField.OtpCode -> R.string.quick_action_copy_security_code
                CopyField.FirstName -> R.string.quick_action_copy_first_name
                CopyField.LastName -> R.string.quick_action_copy_last_name
                CopyField.MiddleName -> R.string.quick_action_copy_middle_name
                CopyField.CompanyName -> R.string.quick_action_copy_company_name
                CopyField.CompanyTitle -> R.string.quick_action_copy_company_title
                CopyField.JustEmail -> R.string.quick_action_copy_email
                CopyField.PhoneNumber -> R.string.quick_action_copy_number
                CopyField.PersonalWebsite -> R.string.quick_action_copy_website
                CopyField.TaxOnlineNumber -> R.string.quick_action_copy_tax_online_number
                CopyField.BankAccountRoutingNumber -> R.string.quick_action_copy_routing_number
                CopyField.BankAccountSortCode -> R.string.quick_action_copy_sort_code
                CopyField.BankAccountAccountNumber -> R.string.quick_action_copy_account_number
                CopyField.BankAccountClabe -> R.string.quick_action_copy_clabe
            }
        }

    companion object {
        

        fun createActionIfFieldExist(
            summaryObject: SummaryObject,
            copyField: CopyField,
            vaultItemFieldContentService: VaultItemFieldContentService,
            itemListContext: ItemListContext
        ): QuickActionCopy? {
            if (vaultItemFieldContentService.hasContent(summaryObject, copyField) &&
                hasCopyRight(copyField, summaryObject)
            ) {
                return QuickActionCopy(summaryObject, itemListContext, copyField)
            }
            return null
        }

        private fun hasCopyRight(copyField: CopyField, summaryObject: SummaryObject) =
            copyField != CopyField.Password || SingletonProvider.getSharingPolicyDataProvider()
                .canEditItem(summaryObject, false)
    }
}