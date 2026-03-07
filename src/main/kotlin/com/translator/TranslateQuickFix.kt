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
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater({
            if (project.isDisposed || !element.isValid) return@invokeLater

            val factory = com.intellij.refactoring.RefactoringFactory.getInstance(project)
            val renameRefactoring = factory.createRename(element, translatedText)

            renameRefactoring.run()
        }, com.intellij.openapi.application.ModalityState.defaultModalityState())
    }

    override fun startInWriteAction(): Boolean = true
}