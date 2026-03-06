package com.translator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

class TranslationAnnotator : ExternalAnnotator<List<PsiNameIdentifierOwner>, Map<PsiNameIdentifierOwner, String>>() {

    companion object {
        private val translationCache = mutableMapOf<String, String>()
        private val alreadyEnglishCache = mutableSetOf<String>()
    }

    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): List<PsiNameIdentifierOwner> {
        return PsiTreeUtil.findChildrenOfType(file, PsiNameIdentifierOwner::class.java).toList()
    }

    override fun doAnnotate(elements: List<PsiNameIdentifierOwner>?): Map<PsiNameIdentifierOwner, String> {
        if (elements.isNullOrEmpty()) return emptyMap()

        val service = ApplicationManager.getApplication().getService(GroqTranslationService::class.java)
        val results = mutableMapOf<PsiNameIdentifierOwner, String>()

        for (element in elements) {
            // POPRAWKA: Odczyt nazwy elementu owinięty w bezpieczny ReadAction
            val word = ApplicationManager.getApplication().runReadAction<String?> {
                if (element.isValid) element.name else null
            }

            // Jeśli element został usunięty lub nazwa jest za krótka - pomijamy
            if (word == null || word.length < 3) continue

            // Optymalizacja z pamięci podręcznej (Cache)
            if (alreadyEnglishCache.contains(word)) continue
            if (translationCache.containsKey(word)) {
                results[element] = translationCache[word]!!
                continue
            }

            // Odpytanie API Groq
            val translated = service.translateToEnglish(word, "Polish")

            if (translated.isNullOrBlank() || translated.equals(word, ignoreCase = true)) {
                alreadyEnglishCache.add(word)
            } else {
                translationCache[word] = translated
                results[element] = translated
            }

            // Delikatne opóźnienie dla darmowego klucza API, aby uniknąć błędów 429 Too Many Requests
            Thread.sleep(150)
        }

        return results
    }

    override fun apply(file: PsiFile, results: Map<PsiNameIdentifierOwner, String>?, holder: AnnotationHolder) {
        if (results.isNullOrEmpty()) return

        for ((element, translated) in results) {
            // Metoda apply() ma gwarantowany odczyt przez IntelliJ, tu runReadAction nie jest wymagane
            val identifier = element.nameIdentifier ?: continue

            holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Translation available: $translated")
                .range(identifier.textRange)
                .withFix(TranslateQuickFix(element, translated))
                .create()
        }
    }
}