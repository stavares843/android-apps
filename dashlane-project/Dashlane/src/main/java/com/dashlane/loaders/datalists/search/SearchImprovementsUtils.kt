package com.dashlane.loaders.datalists.search

import android.content.Context
import com.dashlane.ext.application.KnownLinkedDomains
import com.dashlane.search.ItemType
import com.dashlane.search.Match
import com.dashlane.search.MatchPosition
import com.dashlane.search.SearchField
import com.dashlane.search.SearchableSettingItem
import com.dashlane.search.fields.AddressField
import com.dashlane.search.fields.BankStatementField
import com.dashlane.search.fields.CompanyField
import com.dashlane.search.fields.CredentialField
import com.dashlane.search.fields.CreditCardField
import com.dashlane.search.fields.DriverLicenceField
import com.dashlane.search.fields.EmailField
import com.dashlane.search.fields.FiscalStatementField
import com.dashlane.search.fields.IdCardField
import com.dashlane.search.fields.IdentityField
import com.dashlane.search.fields.PassportField
import com.dashlane.search.fields.PaypalField
import com.dashlane.search.fields.PersonalWebsiteField
import com.dashlane.search.fields.PhoneField
import com.dashlane.search.fields.SecureNoteField
import com.dashlane.search.fields.SettingSearchField
import com.dashlane.search.fields.SocialSecurityStatementField
import com.dashlane.ui.adapters.text.factory.AddressListTextFactory
import com.dashlane.ui.adapters.text.factory.AuthentifiantListTextFactory
import com.dashlane.ui.adapters.text.factory.BankStatementListTextFactory
import com.dashlane.ui.adapters.text.factory.CompanyListTextFactory
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextResolver
import com.dashlane.ui.adapters.text.factory.DriverLicenceListTextFactory
import com.dashlane.ui.adapters.text.factory.EmailListTextFactory
import com.dashlane.ui.adapters.text.factory.FiscalStatementListTextFactory
import com.dashlane.ui.adapters.text.factory.IdCardListTextFactory
import com.dashlane.ui.adapters.text.factory.IdentityListTextFactory
import com.dashlane.ui.adapters.text.factory.PassportListTextFactory
import com.dashlane.ui.adapters.text.factory.PaymentCreditCardListTextFactory
import com.dashlane.ui.adapters.text.factory.PaymentPaypalListTextFactory
import com.dashlane.ui.adapters.text.factory.PersonalWebsiteListTextFactory
import com.dashlane.ui.adapters.text.factory.PhoneListTextFactory
import com.dashlane.ui.adapters.text.factory.SecureNoteListTextFactory
import com.dashlane.ui.adapters.text.factory.SocialSecurityStatementListTextFactory
import com.dashlane.url.toUrlDomain
import com.dashlane.util.tryOrNull
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.IdentityUtil

class SearchImprovementsUtils(
    private val textResolver: DataIdentifierListTextResolver,
    private val identityUtil: IdentityUtil,
    private val userFeaturesChecker: UserFeaturesChecker
) {

    

    @Suppress("LongMethod")
    fun getFilter(context: Context): Map<Any, List<(Any, String) -> Match?>> = mapOf(

        
        ItemType.CREDENTIAL to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.Authentifiant).text.toMatchOrNull(
                    query,
                    CredentialField.TITLE
                )
            },
            { _, query ->
                context.getString(AuthentifiantListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, CredentialField.ITEM_TYPE_NAME)
            },
            { item, query -> item.asCredential()?.email?.toMatchOrNull(query, CredentialField.EMAIL) },
            { item, query -> item.asCredential()?.login?.toMatchOrNull(query, CredentialField.LOGIN) },
            { item, query ->
                val domain = tryOrNull { item.asCredential()?.url?.toUrlDomain() }
                    ?: return@listOf null
                KnownLinkedDomains.getMatchingLinkedDomainSet(domain.value)?.minus(domain)?.firstNotNullOfOrNull {
                    it.value.toMatchOrNull(query, CredentialField.LINKED_DOMAIN)
                }
            },
            { item, query ->
                
                if (userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.LINKED_WEBSITES)) {
                    item.asCredential()?.linkedServices?.associatedDomains?.firstNotNullOfOrNull {
                        it.domain.toMatchOrNull(query, CredentialField.BUNDLE_WEBSITES)
                    }
                } else {
                    null
                }
            },
            { item, query -> item.asCredential()?.note?.toMatchOrNull(query, CredentialField.NOTE) },
            { item, query ->
                item.asCredential()
                    ?.secondaryLogin?.toMatchOrNull(query, CredentialField.SECONDARY_LOGIN)
            },
            { item, query ->
                tryOrNull { item.asCredential()?.url?.toUrlDomain() }?.value?.toMatchOrNull(query, CredentialField.URL)
            },
            { item, query ->
                item.asCredential()
                    ?.userSelectedUrl?.toMatchOrNull(query, CredentialField.USER_SELECTED_URL)
            }
        ),

        
        ItemType.BANK_STATEMENT to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.BankStatement).text.toMatchOrNull(
                    query,
                    BankStatementField.TITLE
                )
            },
            { _, query ->
                context.getString(BankStatementListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, BankStatementField.ITEM_TYPE_NAME)
            },
            { item, query ->
                item.asBankStatement()?.bankAccountBank?.toMatchOrNull(
                    query,
                    BankStatementField.BANK
                )
            },
            { item, query ->
                item.asBankStatement()?.bankAccountOwner?.toMatchOrNull(
                    query,
                    BankStatementField.OWNER
                )
            }
        ),

        ItemType.CREDIT_CARD to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.PaymentCreditCard).text.toMatchOrNull(
                    query,
                    CreditCardField.TITLE
                )
            },
            { _, query ->
                context.getString(PaymentCreditCardListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, CreditCardField.ITEM_TYPE_NAME)
            },
            { item, query -> item.asCreditCard()?.bank?.toMatchOrNull(query, CreditCardField.BANK) },
            { item, query -> item.asCreditCard()?.ownerName?.toMatchOrNull(query, CreditCardField.OWNER) }
        ),

        ItemType.PAYPAL to listOf(
            { item, query -> item.asPaypal()?.name?.toMatchOrNull(query, PaypalField.TITLE) },
            { _, query ->
                context.getString(PaymentPaypalListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, PaypalField.ITEM_TYPE_NAME)
            }
        ),

        
        ItemType.SECURE_NOTE to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.SecureNote).text.toMatchOrNull(
                    query,
                    SecureNoteField.TITLE
                )
            },
            { _, query ->
                context.getString(SecureNoteListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, SecureNoteField.ITEM_TYPE_NAME)
            },
            { item, query ->
                item.asSecureNote()?.let {
                    if (it.secured == false) {
                        it.content?.toMatchOrNull(query, SecureNoteField.CONTENT)
                    } else {
                        null
                    }
                }
            }),

        
        ItemType.DRIVER_LICENCE to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.DriverLicence).text
                    .toMatchOrNull(query, DriverLicenceField.TITLE)
            },
            { _, query ->
                context.getString(DriverLicenceListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, DriverLicenceField.ITEM_TYPE_NAME)
            },
            { item, query -> item.asDriverLicence()?.fullname?.toMatchOrNull(query, DriverLicenceField.FULL_NAME) }
        ),

        ItemType.FISCAL_STATEMENT to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.FiscalStatement).text
                    .toMatchOrNull(query, FiscalStatementField.TITLE)
            },
            { _, query ->
                context.getString(FiscalStatementListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, FiscalStatementField.ITEM_TYPE_NAME)
            },
            { item, query ->
                item.asFiscalStatement()?.let { identityUtil.getOwner(it) }
                    ?.toMatchOrNull(query, FiscalStatementField.FULL_NAME)
            }),

        ItemType.ID_CARD to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.IdCard).text
                    .toMatchOrNull(query, IdCardField.TITLE)
            },
            { _, query ->
                context.getString(IdCardListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, IdCardField.ITEM_TYPE_NAME)
            },
            { item, query -> item.asIdCard()?.fullname?.toMatchOrNull(query, IdCardField.FULL_NAME) },
        ),

        ItemType.PASSPORT to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.Passport).text
                    .toMatchOrNull(query, PassportField.TITLE)
            },
            { _, query ->
                context.getString(PassportListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, PassportField.ITEM_TYPE_NAME)
            },
            { item, query -> item.asPassport()?.fullname?.toMatchOrNull(query, PassportField.FULL_NAME) }
        ),

        ItemType.SOCIAL_SECURITY_STATEMENT to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.SocialSecurityStatement).text
                    .toMatchOrNull(query, SocialSecurityStatementField.TITLE)
            },
            { _, query ->
                context.getString(SocialSecurityStatementListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, SocialSecurityStatementField.ITEM_TYPE_NAME)
            },
            { item, query ->
                item.asSocialSecurityStatement()?.socialSecurityFullname
                    ?.toMatchOrNull(query, SocialSecurityStatementField.SOCIAL_SECURITY_FULL_NAME)
            }
        ),

        
        ItemType.ADDRESS to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.Address).text
                    .toMatchOrNull(query, AddressField.TITLE)
            },
            { _, query ->
                context.getString(AddressListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, AddressField.ITEM_TYPE_NAME)
            },
            { item, query -> item.asAddress()?.addressFull?.toMatchOrNull(query, AddressField.FULL) },
            { item, query -> item.asAddress()?.building?.toMatchOrNull(query, AddressField.BUILDING) },
            { item, query -> item.asAddress()?.city?.toMatchOrNull(query, AddressField.CITY) },
            { item, query -> item.asAddress()?.door?.toMatchOrNull(query, AddressField.DOOR) },
            { item, query -> item.asAddress()?.floor?.toMatchOrNull(query, AddressField.FLOOR) },
            { item, query -> item.asAddress()?.streetName?.toMatchOrNull(query, AddressField.STREET_NAME) },
            { item, query -> item.asAddress()?.zipCode?.toMatchOrNull(query, AddressField.ZIP) }
        ),
        ItemType.COMPANY to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.Company).text
                    .toMatchOrNull(query, CompanyField.TITLE)
            },
            { _, query ->
                context.getString(CompanyListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, CompanyField.ITEM_TYPE_NAME)
            },
            { item, query -> item.asCompany()?.jobTitle?.toMatchOrNull(query, CompanyField.JOB_TITLE) }
        ),
        ItemType.EMAIL to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.Email).text
                    .toMatchOrNull(query, EmailField.TITLE)
            },
            { _, query ->
                context.getString(EmailListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, EmailField.ITEM_TYPE_NAME)
            },
            { item, query -> item.asEmail()?.email?.toMatchOrNull(query, EmailField.EMAIL) }
        ),
        ItemType.IDENTITY to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.Identity).text
                    .toMatchOrNull(query, IdentityField.TITLE)
            },
            { _, query ->
                context.getString(IdentityListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, IdentityField.ITEM_TYPE_NAME)
            },
            { item, query -> item.asIdentity()?.middleName?.toMatchOrNull(query, IdentityField.MIDDLE_NAME) },
            { item, query -> item.asIdentity()?.lastName?.toMatchOrNull(query, IdentityField.LAST_NAME) },
            { item, query -> item.asIdentity()?.pseudo?.toMatchOrNull(query, IdentityField.PSEUDO) }
        ),
        ItemType.PERSONAL_WEBSITE to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.PersonalWebsite).text
                    .toMatchOrNull(query, PersonalWebsiteField.TITLE)
            },
            { _, query ->
                context.getString(PersonalWebsiteListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, PersonalWebsiteField.ITEM_TYPE_NAME)
            },
            { item, query -> item.asPersonalWebsite()?.website?.toMatchOrNull(query, PersonalWebsiteField.WEBSITE) }
        ),
        ItemType.PHONE_NUMBER to listOf(
            { item, query ->
                textResolver.getLine1(context, item as SummaryObject.Phone).text
                    .toMatchOrNull(query, PhoneField.TITLE)
            },
            { _, query ->
                context.getString(PhoneListTextFactory.ITEM_TYPE_NAME_ID)
                    .toMatchOrNull(query, PhoneField.ITEM_TYPE_NAME)
            },
        ),

        
        ItemType.SETTING to listOf { item, query ->
            item.asSetting()?.getSettingTitle()?.toMatchOrNull(query, SettingSearchField.TITLE)
            item.asSetting()?.getSettingDescription()?.toMatchOrNull(query, SettingSearchField.DESCRIPTION)
        }
    )

    private fun String.toMatchOrNull(query: String, searchField: SearchField<*>): Match? {
        val index = this.indexOf(string = query, ignoreCase = true)
        return when {
            index == 0 -> {
                Match(MatchPosition.START, searchField)
            }
            index > 0 -> {
                Match(MatchPosition.ANYWHERE, searchField)
            }
            else -> {
                null
            }
        }
    }

    private fun Any.asCredential(): SummaryObject.Authentifiant? = this as? SummaryObject.Authentifiant
    private fun Any.asBankStatement(): SummaryObject.BankStatement? = this as? SummaryObject.BankStatement
    private fun Any.asCreditCard(): SummaryObject.PaymentCreditCard? = this as? SummaryObject.PaymentCreditCard
    private fun Any.asPaypal(): SummaryObject.PaymentPaypal? = this as? SummaryObject.PaymentPaypal
    private fun Any.asSecureNote(): SummaryObject.SecureNote? = this as? SummaryObject.SecureNote
    private fun Any.asDriverLicence(): SummaryObject.DriverLicence? = this as? SummaryObject.DriverLicence
    private fun Any.asIdCard(): SummaryObject.IdCard? = this as? SummaryObject.IdCard
    private fun Any.asFiscalStatement(): SummaryObject.FiscalStatement? = this as? SummaryObject.FiscalStatement
    private fun Any.asPassport(): SummaryObject.Passport? = this as? SummaryObject.Passport
    private fun Any.asSocialSecurityStatement(): SummaryObject.SocialSecurityStatement? =
        this as? SummaryObject.SocialSecurityStatement

    private fun Any.asAddress(): SummaryObject.Address? = this as? SummaryObject.Address
    private fun Any.asCompany(): SummaryObject.Company? = this as? SummaryObject.Company
    private fun Any.asEmail(): SummaryObject.Email? = this as? SummaryObject.Email
    private fun Any.asIdentity(): SummaryObject.Identity? = this as? SummaryObject.Identity
    private fun Any.asPersonalWebsite(): SummaryObject.PersonalWebsite? = this as? SummaryObject.PersonalWebsite
    private fun Any.asSetting(): SearchableSettingItem? = this as? SearchableSettingItem
}