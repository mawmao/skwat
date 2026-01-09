package com.humayapp.scout.feature.form.impl.ui.util

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.insert


object ScoutOutputTransformations {
    val PhoneNumber = OutputTransformation {
        if (length > 4) insert(4, " ")
        if (length > 7) insert(8, " ")
    }
    val Decimal = OutputTransformation {
        if (asCharSequence().isNotEmpty() && asCharSequence().first() == '.') {
            insert(0, "0")
        }
    }

    val DecimalOrNA = OutputTransformation {
        val text = asCharSequence().toString()

        if (text.equals("N/A", ignoreCase = true)) {
            if (text != "N/A") {
                replace(0, length, "N/A")
            }
            return@OutputTransformation
        }

        if (text.isNotEmpty() && text.first() == '.') {
            insert(0, "0")
        }
    }

    val Percentage = OutputTransformation {
        val text = asCharSequence().toString()
        if (text.isEmpty()) return@OutputTransformation
        if (text.first() == '.') {
            insert(0, "0")
        }
        if (!text.endsWith("%")) {
            insert(length, "%")
        }
    }
}
