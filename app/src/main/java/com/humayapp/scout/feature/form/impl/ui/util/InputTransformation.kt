package com.humayapp.scout.feature.form.impl.ui.util

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then

object ScoutInputTransformations {
    val NoLeadingZero = InputTransformation {
        if (asCharSequence().startsWith('0')) {
            if (length == 1) {
                revertAllChanges()
            }
        }
    }
    val MaxLength2Digits: InputTransformation = InputTransformation.maxLength(2)
    val MaxLength3Digits: InputTransformation = InputTransformation.maxLength(3)

    val Name: InputTransformation = InputTransformation {
        val text = asCharSequence()
        val len = text.length

        if (len == 0) return@InputTransformation

        if (len == 1 && text[0].isWhitespace()) {
            revertAllChanges()
            return@InputTransformation
        }

        if (len > 50 || text.any { c -> !c.isLetter() && c !in setOf(' ', '-', '\'') }) {
            revertAllChanges()
            return@InputTransformation
        }

        if (len >= 2 && text[len - 1].isWhitespace() && text[len - 2].isWhitespace()) {
            revertAllChanges()
            return@InputTransformation
        }

        val lastCharIndex = len - 1
        val lastChar = text[lastCharIndex]
        val prevCharIsSpace = lastCharIndex == 0 || text[lastCharIndex - 1].isWhitespace()

        if (lastChar.isLowerCase() && prevCharIsSpace) {
            replace(lastCharIndex, lastCharIndex + 1, lastChar.uppercaseChar().toString())
        }
    }
    val PhoneNumber = InputTransformation {
        val currentText = asCharSequence()

        if (length == 1) {
            val firstChar = currentText[0]
            if (firstChar in '1'..'9') {
                replace(0, 1, "09$firstChar")
                return@InputTransformation
            }
        }

        if (length == 2) {
            if (currentText[0] == '0' && currentText[1] != '9') {
                replace(1, 2, "9")
                return@InputTransformation
            }
        }

        if (length > 11) {
            revertAllChanges()
            return@InputTransformation
        }

        if (currentText.any { !it.isDigit() }) {
            revertAllChanges()
            return@InputTransformation
        }

        if (length >= 1 && currentText[0] != '0') {
            revertAllChanges()
            return@InputTransformation
        }

        if (length >= 2 && currentText[1] != '9') {
            revertAllChanges()
            return@InputTransformation
        }
    }

    val Decimal = InputTransformation {
        val currentText = asCharSequence()

        if (currentText.isEmpty()) return@InputTransformation

        if (!currentText.all { it.isDigit() || it == '.' } || currentText.count { it == '.' } > 1) {
            revertAllChanges()
            return@InputTransformation
        }

        if (currentText.length > 1 && currentText[0] == '0' && currentText[1].isDigit()) {
            revertAllChanges()
            return@InputTransformation
        }
    }

    val DecimalOrNA = InputTransformation {
        val text = asCharSequence().toString()

        if (text.isEmpty()) return@InputTransformation

        if (text.equals("N/A", ignoreCase = true)) {
            if (text != "N/A") {
                replace(0, length, "N/A")
            }
            return@InputTransformation
        }

        if (!text.all { it.isDigit() || it == '.' } || text.count { it == '.' } > 1) {
            revertAllChanges()
            return@InputTransformation
        }

        if (text.length > 1 && text[0] == '0' && text[1].isDigit()) {
            revertAllChanges()
            return@InputTransformation
        }
    }

    val Whole = InputTransformation {
        val currentText = asCharSequence()

        if (currentText.isEmpty()) return@InputTransformation
        if (!currentText.all { it.isDigit() }) {
            revertAllChanges()
            return@InputTransformation
        }

        if (currentText.length > 1 && currentText[0] == '0') {
            revertAllChanges()
            return@InputTransformation
        }
    }

    val Percentage = Decimal.then {
        val currentText = asCharSequence().toString()
        val dotIndex = currentText.indexOf('.')
        if (dotIndex != -1 && currentText.length - dotIndex - 1 > 2) {
            replace(0, currentText.length, currentText.take(dotIndex + 3))
        }
    }
}
