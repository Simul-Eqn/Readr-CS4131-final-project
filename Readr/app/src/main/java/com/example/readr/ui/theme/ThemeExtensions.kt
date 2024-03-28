package com.example.readr.ui.theme


import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Typeface
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

data class TextStyles(
    val xs: TextStyle = TextStyle(fontSize=TextSizes().xs*Variables.textScale, fontFamily=Variables.textFontFamily),
    val s: TextStyle = TextStyle(fontSize=TextSizes().s*Variables.textScale, fontFamily=Variables.textFontFamily),
    val m: TextStyle = TextStyle(fontSize=TextSizes().m*Variables.textScale, fontFamily=Variables.textFontFamily),
    val l: TextStyle = TextStyle(fontSize=TextSizes().l*Variables.textScale, fontFamily=Variables.textFontFamily),
    val xl: TextStyle = TextStyle(fontSize=TextSizes().xl*Variables.textScale, fontFamily=Variables.textFontFamily),
)

val LocalTextStyles = compositionLocalOf { TextStyles() }



data class ReplacedTextStyles(
    val xs: TextStyle = TextStyle(fontSize=TextSizes().xs*Variables.textScale, fontFamily=Variables.overlayFontFamily),
    val s: TextStyle = TextStyle(fontSize=TextSizes().s*Variables.textScale, fontFamily=Variables.overlayFontFamily),
    val m: TextStyle = TextStyle(fontSize=TextSizes().m*Variables.textScale, fontFamily=Variables.overlayFontFamily),
    val l: TextStyle = TextStyle(fontSize=TextSizes().l*Variables.textScale, fontFamily=Variables.overlayFontFamily),
    val xl: TextStyle = TextStyle(fontSize=TextSizes().xl*Variables.textScale, fontFamily=Variables.overlayFontFamily),
)

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


