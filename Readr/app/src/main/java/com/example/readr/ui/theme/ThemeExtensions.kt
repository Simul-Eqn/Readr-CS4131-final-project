package com.example.readr.ui.theme


import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readr.openDyslexic

data class TextSizes(
    val xs: TextUnit = 10.sp,
    val s: TextUnit = 12.sp,
    val m: TextUnit = 15.sp,
    val l: TextUnit = 18.sp,
    val xl: TextUnit = 20.sp,
)

val LocalTextSizes = compositionLocalOf { TextSizes() }


data class TextStyles(
    val xs: TextStyle = TextStyle(fontSize=TextSizes().xs),
    val s: TextStyle = TextStyle(fontSize=TextSizes().s),
    val m: TextStyle = TextStyle(fontSize=TextSizes().m),
    val l: TextStyle = TextStyle(fontSize=TextSizes().xl),
    val xl: TextStyle = TextStyle(fontSize=TextSizes().xl),
)

val LocalTextStyles = compositionLocalOf { TextStyles() }


data class ReplacedTextStyles(
    val xs: TextStyle = TextStyle(fontSize=TextSizes().xs, fontFamily=openDyslexic),
    val s: TextStyle = TextStyle(fontSize=TextSizes().s, fontFamily=openDyslexic),
    val m: TextStyle = TextStyle(fontSize=TextSizes().m, fontFamily=openDyslexic),
    val l: TextStyle = TextStyle(fontSize=TextSizes().xl, fontFamily=openDyslexic),
    val xl: TextStyle = TextStyle(fontSize=TextSizes().xl, fontFamily=openDyslexic),
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


