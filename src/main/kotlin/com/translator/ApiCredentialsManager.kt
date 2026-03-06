package com.translator

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

object ApiCredentialsManager {
    private const val SUBSYSTEM = "TranslationPlugin"
    private const val KEY = "ApiKey"

    private val credentialAttributes = CredentialAttributes(
        generateServiceName(SUBSYSTEM, KEY)
    )

    var apiKey: String?
        get() = PasswordSafe.instance.getPassword(credentialAttributes)
        set(value) {
            val credentials = if (value.isNullOrBlank()) null else Credentials("", value)
            PasswordSafe.instance.set(credentialAttributes, credentials)
        }
}