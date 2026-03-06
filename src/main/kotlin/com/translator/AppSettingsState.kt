package com.translator

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*

@State(
    name = "org.example.plugin.AppSettingsState",
    storages = [Storage("TranslationPluginSettings.xml")]
)
@Service(Service.Level.APP)
class AppSettingsState : PersistentStateComponent<AppSettingsState> {

    // Tu dodajesz swoje parametry
    var targetLanguage: String = "English"
    var modelName: String = "gpt-4"

    override fun getState(): AppSettingsState = this

    override fun loadState(state: AppSettingsState) {
        this.targetLanguage = state.targetLanguage
        this.modelName = state.modelName
    }

    companion object {
        val instance: AppSettingsState
            get() = ApplicationManager.getApplication().getService(AppSettingsState::class.java)
    }
}