package com.translator

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement

class TranslateQuickFix(
    private val element: PsiNamedElement,
    private val translatedText: String
) : IntentionAction {

    override fun getText(): String = "Translate to '$translatedText'"

    override fun getFamilyName(): String = "Translation Plugin"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = element.isValid

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        element.setName(translatedText)
    }

    override fun startInWriteAction(): Boolean = true
}