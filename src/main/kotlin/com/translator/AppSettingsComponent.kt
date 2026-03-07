package com.translator

import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

class AppSettingsComponent {
    val panel: JPanel
    private val apiKeyField = JBPasswordField()
    private val modelNameField = JBTextField()
    private val targetLanguageField = JBTextField()

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Klucz API: ", apiKeyField, 1, false)
            .addLabeledComponent("Nazwa modelu LLM: ", modelNameField, 1, false)
            .addLabeledComponent("Język docelowy ", targetLanguageField, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    var apiKey: String?
        get() = String(apiKeyField.password)
        set(value) {
            apiKeyField.text = value
        }

    var modelName: String
        get() = modelNameField.text
        set(value) {
            modelNameField.text = value
        }

    var targetLanguage: String
        get() = targetLanguageField.text
        set(value) {
            targetLanguageField.text = value
        }
}