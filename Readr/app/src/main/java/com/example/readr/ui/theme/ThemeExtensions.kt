package com.example.readr.ui.theme


import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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


data class Spacings(
    val xs: Dp = 3.dp,
    val s: Dp = 5.dp,
    val m: Dp = 8.dp,
    val l: Dp = 12.dp,
    val xl: Dp = 15.dp,
)

val LocalSpacings = compositionLocalOf { Spacings() }



