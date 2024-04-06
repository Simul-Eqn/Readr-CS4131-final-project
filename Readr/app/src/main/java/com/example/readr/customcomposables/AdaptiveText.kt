package com.example.readr.customcomposables

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle

class AdaptiveText(val text:String, val initStyle: TextStyle, val maxLines:Int, val softWrap:Boolean,
                   val modifier: Modifier=Modifier, val showAnyways:Boolean = false) {
    @Composable
    fun ShowOverflowWidth() { // if maxLines=1
        var titleTextStyle by remember { mutableStateOf(initStyle) }
        var displayTitle by remember { mutableStateOf(false) }


        Text(
            text,
            modifier = modifier.wrapContentWidth()
                .drawWithContent{ if (displayTitle or showAnyways) drawContent() }
                .alpha(if (displayTitle) 1.0f else 0.0f),
            style= titleTextStyle,
            maxLines=maxLines,
            softWrap=softWrap,
            onTextLayout = {
                if (it.didOverflowWidth) {
                    titleTextStyle = titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                } else {
                    displayTitle = true
                }
            }
        )
    }

    @Composable
    fun ShowOverflowHeight() { // if maxLines != 1
        var titleTextStyle by remember { mutableStateOf(initStyle) }
        var displayTitle by remember { mutableStateOf(false) }


        Text(
            text,
            modifier = modifier.wrapContentHeight()
                .drawWithContent{ if (displayTitle or showAnyways) drawContent() }
                .alpha(if (displayTitle) 1.0f else 0.0f),
            style= titleTextStyle,
            maxLines=maxLines,
            softWrap=softWrap,
            onTextLayout = {
                if (it.didOverflowHeight) {
                    titleTextStyle = titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                } else {
                    displayTitle = true
                }
            }
        )
    }

    @Composable
    fun ShowOverflow() {
        var titleTextStyle by remember { mutableStateOf(initStyle) }
        var displayTitle by remember { mutableStateOf(false) }


        Text(
            text,
            modifier = modifier.wrapContentWidth().wrapContentHeight().drawWithContent{ if (displayTitle or showAnyways) drawContent() },
            style= titleTextStyle,
            maxLines=maxLines,
            softWrap=softWrap,
            onTextLayout = {
                if (it.didOverflowWidth or it.didOverflowHeight) {
                    titleTextStyle = titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                } else {
                    displayTitle = true
                }
            }
        )
    }
}


class AnnotatedAdaptiveText(val text: AnnotatedString, val initStyle: TextStyle, val maxLines:Int, val softWrap:Boolean, val modifier: Modifier=Modifier, val showAnyways:Boolean=false ) {
    @Composable
    fun ShowOverflowWidth() { // if maxLines=1
        var titleTextStyle by remember { mutableStateOf(initStyle) }
        var displayTitle by remember { mutableStateOf(false) }


        Text(
            text,
            modifier = modifier.wrapContentWidth().drawWithContent{ if (displayTitle or showAnyways) drawContent() },
            style= titleTextStyle,
            maxLines=maxLines,
            softWrap=softWrap,
            onTextLayout = {
                if (it.didOverflowWidth) {
                    titleTextStyle = titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                } else {
                    displayTitle = true
                }
            }
        )
    }

    @Composable
    fun ShowOverflowHeight() { // if maxLines != 1
        var titleTextStyle by remember { mutableStateOf(initStyle) }
        var displayTitle by remember { mutableStateOf(false) }


        Text(
            text,
            modifier = modifier.wrapContentHeight().drawWithContent{ if (displayTitle or showAnyways) drawContent() },
            style= titleTextStyle,
            maxLines=maxLines,
            softWrap=softWrap,
            onTextLayout = {
                if (it.didOverflowHeight) {
                    titleTextStyle = titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                } else {
                    displayTitle = true
                }
            }
        )
    }

    @Composable
    fun ShowOverflow() {
        var titleTextStyle by remember { mutableStateOf(initStyle) }
        var displayTitle by remember { mutableStateOf(false) }


        Text(
            text,
            modifier = modifier.wrapContentWidth().wrapContentHeight().drawWithContent{ if (displayTitle or showAnyways) drawContent() },
            style= titleTextStyle,
            maxLines=maxLines,
            softWrap=softWrap,
            onTextLayout = {
                if (it.didOverflowWidth or it.didOverflowHeight) {
                    titleTextStyle = titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                } else {
                    displayTitle = true
                }
            }
        )
    }
}

