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

    // Tekst wyświetlany w menu po wciśnięciu Alt+Enter
    override fun getText(): String = "Translate to '$translatedText'"

    override fun getFamilyName(): String = "Translation Plugin"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = element.isValid

    // To wykona się po kliknięciu opcji
    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        element.setName(translatedText) // Zmienia nazwę zmiennej/funkcji
    }

    // Pozwala IntelliJ na bezpieczną modyfikację pliku
    override fun startInWriteAction(): Boolean = true
}