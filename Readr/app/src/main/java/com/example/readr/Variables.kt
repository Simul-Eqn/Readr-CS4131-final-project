package com.example.readr

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

class Variables {
    companion object {
        var overlayTextSize = 20f
        var overlayFontFamily = openDyslexic
        var textScale = 1.0F
        var textFontFamily = openDyslexic
    }
}

val openDyslexic = FontFamily(
    Font(R.font.opendyslexic3_normal, FontWeight.Normal),
    Font(R.font.opendyslexic3_bold, FontWeight.Bold),
)

