package com.translator

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    // Klucz API trzymamy w pamięci — ładujemy go raz, w tle
    private var cachedApiKey: String = ""

    override fun getDisplayName(): String = "Translation Plugin"

    override fun createComponent(): JComponent {
        mySettingsComponent = AppSettingsComponent()

        // Ładujemy klucz API asynchronicznie (poza EDT), żeby nie blokować UI
        ApplicationManager.getApplication().executeOnPooledThread {
            val key = ApiCredentialsManager.apiKey ?: ""
            cachedApiKey = key
            // Wracamy na EDT żeby zaktualizować pole w formularzu
            ApplicationManager.getApplication().invokeLater {
                mySettingsComponent?.apiKey = cachedApiKey
            }
        }

        return mySettingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings = AppSettingsState.instance
        val formApiKey = mySettingsComponent?.apiKey ?: ""
        return mySettingsComponent?.modelName != settings.modelName ||
                cachedApiKey != formApiKey
    }

    override fun apply() {
        val settings = AppSettingsState.instance
        settings.modelName = mySettingsComponent?.modelName ?: "llama3-8b-8192"

        val newKey = mySettingsComponent?.apiKey ?: ""
        cachedApiKey = newKey

        // Zapis do PasswordSafe też robimy w tle
        ApplicationManager.getApplication().executeOnPooledThread {
            ApiCredentialsManager.apiKey = newKey
        }
    }

    override fun reset() {
        val settings = AppSettingsState.instance
        mySettingsComponent?.modelName = settings.modelName
        // Używamy wartości z pamięci — nie czytamy z dysku na EDT
        mySettingsComponent?.apiKey = cachedApiKey
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}