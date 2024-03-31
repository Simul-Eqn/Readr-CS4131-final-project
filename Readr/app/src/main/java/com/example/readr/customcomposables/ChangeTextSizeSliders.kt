package com.example.readr.customcomposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.readr.Variables
import com.example.readr.forceRecomposeWith
import com.example.readr.ui.theme.LocalTextStyles
import kotlin.math.roundToInt

@Composable
fun ChangeReplacedTextSizeSlider(overlayTextSize:Float,
                                 textScale:Float= Variables.textScale, // this is to allow recomposition
                                 extra: Int=0,
                                 setOverlayTextSize:(Float)->Unit, ) {

    var ots by remember { mutableFloatStateOf(overlayTextSize) }

    Column(modifier = Modifier.padding(horizontal=16.dp)
        .forceRecomposeWith(textScale)
        .forceRecomposeWith(overlayTextSize)
        .forceRecomposeWith(extra)) {



        key(textScale, overlayTextSize, extra) {

            val initStyle = LocalTextStyles.current.l
            var titleTextStyle by remember { mutableStateOf(initStyle) }
            var displayTitle by remember { mutableStateOf(false) }

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(background = MaterialTheme.colorScheme.background)) {
                        append("(Overlay) Replaced text size: $ots")
                    }
                },
                modifier = Modifier.forceRecomposeWith(textScale)
                    .forceRecomposeWith(overlayTextSize)
                    .forceRecomposeWith(extra)
                    .wrapContentWidth().drawWithContent{ if (displayTitle) drawContent() },
                style = titleTextStyle,
                maxLines = 1,
                softWrap = false,
                onTextLayout = {
                    if (it.didOverflowWidth) {
                        titleTextStyle =
                            titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                    } else {
                        displayTitle = true
                    }
                }
            )




        }

        Slider(
            value = ots,
            onValueChange = {
                ots = Math.round(it).toFloat(); setOverlayTextSize(Math.round(it).toFloat())
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            steps = 24,
            valueRange = 10f..35f,
            modifier = Modifier
                .forceRecomposeWith(textScale)
                .forceRecomposeWith(overlayTextSize)
                .forceRecomposeWith(extra)
        )


    }
}

@Composable
fun ChangeTextScaleSlider(textScale:Float, setTextScale: (Float)->Unit, extra:Int, addToExtra:()->Unit) {

    Column(
        modifier = Modifier.forceRecomposeWith(textScale).forceRecomposeWith(extra).padding(horizontal=16.dp)
    ) {

        key(textScale, extra) {

            val initStyle = LocalTextStyles.current.l
            var titleTextStyle by remember { mutableStateOf(initStyle) }
            var displayTitle by remember { mutableStateOf(false) }


            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(background = MaterialTheme.colorScheme.background)) {
                        append("(In-app) Text scale: $textScale")
                    }
                },
                modifier = Modifier.forceRecomposeWith(textScale).forceRecomposeWith(extra)
                    .wrapContentWidth().drawWithContent { if (displayTitle) drawContent() },
                style = titleTextStyle,
                maxLines = 1,
                softWrap = false,
                onTextLayout = {
                    if (it.didOverflowWidth) {
                        titleTextStyle =
                            titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                    } else {
                        displayTitle = true
                    }
                }
            )




        }

        Slider(
            value = textScale,
            onValueChange = {

                setTextScale((it * 10).roundToInt().toFloat() / 10.0f)

                addToExtra()
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            steps = 39,
            valueRange = 0.5f..2.5f,
            modifier = Modifier.forceRecomposeWith(extra)
        )

    }
}

