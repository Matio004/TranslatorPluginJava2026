package com.translator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil
import java.util.concurrent.ConcurrentHashMap

class TranslationAnnotator : ExternalAnnotator<List<PsiNameIdentifierOwner>, Map<PsiNameIdentifierOwner, String>>() {

    companion object {
        private val translationCache = ConcurrentHashMap<String, String>()
        private val alreadyEnglishCache = ConcurrentHashMap.newKeySet<String>()
    }

    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): List<PsiNameIdentifierOwner> {
        return PsiTreeUtil.findChildrenOfType(file, PsiNameIdentifierOwner::class.java).toList()
    }

    override fun doAnnotate(elements: List<PsiNameIdentifierOwner>?): Map<PsiNameIdentifierOwner, String> {
        if (elements.isNullOrEmpty()) return emptyMap()

        val apiKey = ApiCredentialsManager.apiKey?.trim()
        if (apiKey.isNullOrBlank()) return emptyMap()

        val service = ApplicationManager.getApplication().getService(GroqTranslationService::class.java)
        val results = mutableMapOf<PsiNameIdentifierOwner, String>()

        for (element in elements) {
            val word = ApplicationManager.getApplication().runReadAction<String?> {
                if (element.isValid) element.name else null
            }

            if (word == null || word.length < 3) continue

            if (alreadyEnglishCache.contains(word)) continue
            if (translationCache.containsKey(word)) {
                results[element] = translationCache[word]!!
                continue
            }

            val translated = service.translateToEnglish(word, "Polish", apiKey)

            if (translated.isNullOrBlank() || translated.equals(word, ignoreCase = true)) {
                alreadyEnglishCache.add(word)
            } else {
                translationCache[word] = translated
                results[element] = translated
            }

            Thread.sleep(150)
        }

        return results
    }

    override fun apply(file: PsiFile, results: Map<PsiNameIdentifierOwner, String>?, holder: AnnotationHolder) {
        if (results.isNullOrEmpty()) return

        for ((element, translated) in results) {

            val identifier = element.nameIdentifier ?: continue

            holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Translation available: $translated")
                .range(identifier.textRange)
                .withFix(TranslateQuickFix(element, translated))
                .create()
        }
    }
}