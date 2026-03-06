package com.translator

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.ui.popup.JBPopupFactory
import java.util.concurrent.Executors

class VariableTypingListener : EditorFactoryListener {

    private val log = Logger.getInstance(VariableTypingListener::class.java)
    private val executor = Executors.newSingleThreadExecutor()

    init {
        log.warn(">>> VariableTypingListener ZAINICJALIZOWANY!")
    }

    override fun editorCreated(event: EditorFactoryEvent) {
        log.warn(">>> VariableTypingListener: editorCreated dla edytora")
        val editor = event.editor
        editor.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(e: DocumentEvent) {
                val newFragment = e.newFragment.toString()

                // Reagujemy tylko po wpisaniu spacji lub nowej linii
                if (newFragment != " " && newFragment != "\n") return

                val offset = e.offset
                if (offset <= 0) return

                val text = editor.document.text
                val wordStart = findWordStart(text, offset)
                if (wordStart >= offset) return

                val word = text.substring(wordStart, offset)
                log.warn(">>> Wykryto słowo: '$word' (długość=${word.length})")

                // Ignorujemy słowa krótsze niż 3 znaki
                if (word.length < 3) return

                // Odpalamy zapytanie do API w osobnym wątku
                executor.submit {
                    checkAndOffer(editor, word, wordStart, offset)
                }
            }
        })
    }

    private fun findWordStart(text: String, offset: Int): Int {
        var i = offset - 1
        while (i >= 0 && (text[i].isLetterOrDigit() || text[i] == '_')) i--
        return i + 1
    }

    private fun checkAndOffer(editor: Any, word: String, wordStart: Int, wordEnd: Int) {
        // Usunęliśmy sprawdzanie polskich znaków (Regex i hasNonAscii).
        // Teraz sprawdzamy KAŻDE wpisane słowo.
        log.warn(">>> checkAndOffer: weryfikuję słowo='$word'")

        val editorImpl = editor as com.intellij.openapi.editor.Editor
        val service = ApplicationManager.getApplication()
            .getService(GroqTranslationService::class.java)

        log.warn(">>> Wywołuję API dla słowa: '$word'")
        val translated = service.translateToEnglish(word, "Polish")

        log.warn(">>> Odpowiedź API: '$translated'")

        // Jeśli tłumaczenie się nie powiodło, albo API zwróciło to samo słowo (czyli było już angielskie) - nic nie robimy
        if (translated.isNullOrBlank() || translated.equals(word, ignoreCase = true)) {
            log.warn(">>> Zignorowano (słowo było już angielskie lub API zwróciło błąd).")
            return
        }

        // Jeśli mamy poprawne tłumaczenie, wracamy na wątek UI i pokazujemy popup
        ApplicationManager.getApplication().invokeLater {
            showTranslationPopup(editorImpl, word, translated, wordStart, wordEnd)
        }
    }

    private fun showTranslationPopup(
        editor: com.intellij.openapi.editor.Editor,
        original: String,
        translated: String,
        wordStart: Int,
        wordEnd: Int
    ) {
        val project = editor.project ?: return
        log.warn(">>> Pokazuję popup: '$original' → '$translated'")

        JBPopupFactory.getInstance()
            .createConfirmation(
                "Translate \"$original\" → \"$translated\"?",
                "Yes, replace",
                "No",
                {
                    WriteCommandAction.runWriteCommandAction(project) {
                        editor.document.replaceString(wordStart, wordEnd, translated)
                    }
                },
                0
            )
            .showInBestPositionFor(editor)
    }
}