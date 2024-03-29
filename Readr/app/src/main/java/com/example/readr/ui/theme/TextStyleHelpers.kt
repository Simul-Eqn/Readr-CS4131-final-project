package com.example.readr.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.readr.Variables

enum class InAppTextStylePrefs(
    val key:String,
    val textStyle: TextStyle
) {
    XS("XS", TextStyle(fontSize=TextSizes().xs* Variables.textScale, fontFamily= Variables.textFontFamily) ),
    S("S", TextStyle(fontSize=TextSizes().s* Variables.textScale, fontFamily= Variables.textFontFamily) ),
    M("M", TextStyle(fontSize=TextSizes().m* Variables.textScale, fontFamily= Variables.textFontFamily) ),
    L("L", TextStyle(fontSize=TextSizes().l* Variables.textScale, fontFamily= Variables.textFontFamily) ),
    XL("XL", TextStyle(fontSize=TextSizes().xl* Variables.textScale, fontFamily= Variables.textFontFamily) );

    companion object {
        fun getFromKey(key:String?) : InAppTextStylePrefs {
            return InAppTextStylePrefs.entries.find { it.key == key } ?: M
        }
    }
}

enum class OverlayTextStylePrefs(
    val key:String,
    val textStyle: TextStyle
) {
    XS("XS", TextStyle(fontSize=( (TextSizes().xs.value/TextSizes().m.value) *Variables.overlayTextSize).sp ,fontFamily= Variables.textFontFamily) ),
    S("S", TextStyle(fontSize=( (TextSizes().s.value/TextSizes().m.value) *Variables.overlayTextSize).sp, fontFamily= Variables.textFontFamily) ),
    M("M", TextStyle(fontSize=( (TextSizes().m.value/TextSizes().m.value) *Variables.overlayTextSize).sp, fontFamily= Variables.textFontFamily) ),
    L("L", TextStyle(fontSize=( (TextSizes().l.value/TextSizes().m.value) *Variables.overlayTextSize).sp, fontFamily= Variables.textFontFamily) ),
    XL("XL", TextStyle(fontSize=( (TextSizes().xl.value/TextSizes().m.value) *Variables.overlayTextSize).sp, fontFamily= Variables.textFontFamily) );

    companion object {
        fun getFromKey(key:String?) : OverlayTextStylePrefs {
            return OverlayTextStylePrefs.entries.find { it.key == key } ?: M
        }
    }
}


class TextStyleHelpers {
}