package com.example.readr.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.example.readr.Variables
import com.example.readr.ui.theme.LocalTextStyles
import com.example.readr.forceRecomposeWith
import kotlin.math.roundToInt

@Composable
fun ChangeReplacedTextSizeSlider(overlayTextSize:Float,
                                 textScale:Float=Variables.textScale, // this is to allow recomposition
                                 setOverlayTextSize:(Float)->Unit) {

    var ots by remember { mutableFloatStateOf(overlayTextSize) }

    Column(modifier = Modifier.forceRecomposeWith(textScale).forceRecomposeWith(overlayTextSize)) {

        Text(buildAnnotatedString {
            withStyle(SpanStyle(background = MaterialTheme.colorScheme.background)) {
                append("(Overlay) Replaced text size: $ots")
            }
        }, style=LocalTextStyles.current.l, modifier = Modifier.forceRecomposeWith(textScale).forceRecomposeWith(overlayTextSize))

        Slider(
            value = ots,
            onValueChange = { ots = Math.round(it).toFloat() ; setOverlayTextSize(Math.round(it).toFloat()) },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            steps = 24,
            valueRange = 10f..35f,
            modifier = Modifier.forceRecomposeWith(textScale).forceRecomposeWith(overlayTextSize)
        )
    }
}

@Composable
fun ChangeTextScaleSlider(textScale:Float, setTextScale: (Float)->Unit) {

    Column {

        Text(buildAnnotatedString {
            withStyle(SpanStyle(background = MaterialTheme.colorScheme.background)) {
                append("(In-app) Text scale: $textScale")
            }
        }, style= LocalTextStyles.current.l)

        Slider(
            value = textScale,
            onValueChange = {
                setTextScale((it * 10).roundToInt().toFloat()/10.0f)
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            steps = 39,
            valueRange = 0.5f..2.5f
        )
    }
}

