package com.example.readr.ui.theme


import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readr.R
import com.example.readr.Variables


data class TextSizes(
    val xs: TextUnit = 10.sp,
    val s: TextUnit = 12.sp,
    val m: TextUnit = 15.sp,
    val l: TextUnit = 18.sp,
    val xl: TextUnit = 20.sp,
)

val LocalTextSizes = compositionLocalOf { TextSizes() }


// same code, makes sure it gets loaded here
val openDyslexic = FontFamily(
    Font(R.font.opendyslexic3_normal, FontWeight.Normal),
    Font(R.font.opendyslexic3_bold, FontWeight.Bold),
)


class TextStyles(
    var textScaleLocal:Float = Variables.textScale,
) {

    lateinit var xs:TextStyle
    lateinit var s:TextStyle
    lateinit var m:TextStyle
    lateinit var l:TextStyle
    lateinit var xl:TextStyle
    init {
        setVals()
    }

    fun setVals() {
        xs = TextStyle(fontSize=TextSizes().xs*textScaleLocal, fontFamily=Variables.textFontFamily)
        s = TextStyle(fontSize=TextSizes().s*textScaleLocal, fontFamily=Variables.textFontFamily)
        m = TextStyle(fontSize=TextSizes().m*textScaleLocal, fontFamily=Variables.textFontFamily)
        l = TextStyle(fontSize=TextSizes().l*textScaleLocal, fontFamily=Variables.textFontFamily)
        xl = TextStyle(fontSize=TextSizes().xl*textScaleLocal, fontFamily=Variables.textFontFamily)
    }

    fun setTextScale(newTextScale:Float) {
        textScaleLocal = newTextScale
        setVals()
    }
}

val LocalTextStyles = compositionLocalOf { TextStyles() }




data class ReplacedTextStyles(
    var overlayTextSizeLocal: Float = Variables.overlayTextSize.toFloat(),
) {

    lateinit var xs:TextStyle
    lateinit var s:TextStyle
    lateinit var m:TextStyle
    lateinit var l:TextStyle
    lateinit var xl:TextStyle

    init {
        setVals()
    }

    fun setVals() {
        xs = TextStyle(fontSize=( (TextSizes().xs.value/TextSizes().m.value) *overlayTextSizeLocal).sp, fontFamily=Variables.overlayFontFamily)
        s = TextStyle(fontSize=( (TextSizes().s.value/TextSizes().m.value) *overlayTextSizeLocal).sp, fontFamily=Variables.overlayFontFamily)
        m = TextStyle(fontSize=( (TextSizes().m.value/TextSizes().m.value) *overlayTextSizeLocal).sp, fontFamily=Variables.overlayFontFamily)
        l = TextStyle(fontSize=( (TextSizes().l.value/TextSizes().m.value) *overlayTextSizeLocal).sp, fontFamily=Variables.overlayFontFamily)
        xl = TextStyle(fontSize=( (TextSizes().xl.value/TextSizes().m.value) *overlayTextSizeLocal).sp, fontFamily=Variables.overlayFontFamily)
    }

    fun setOverlayTextSize(newOverlayTextSize:Float) {
        this.overlayTextSizeLocal = newOverlayTextSize
        setVals()
    }
}


val LocalReplacedTextStyles = compositionLocalOf { ReplacedTextStyles() }


data class Spacings(
    val xs: Dp = 3.dp,
    val s: Dp = 5.dp,
    val m: Dp = 8.dp,
    val l: Dp = 12.dp,
    val xl: Dp = 15.dp,
)

val LocalSpacings = compositionLocalOf { Spacings() }


data class MoreColors(
    val greyed_text: Color = Color.Gray,
    val highlight_text: Color = Color.hsv(255.0F, 0.10F, 0.50F, 0.3F),
)

val LocalMoreColors = compositionLocalOf { MoreColors() }


