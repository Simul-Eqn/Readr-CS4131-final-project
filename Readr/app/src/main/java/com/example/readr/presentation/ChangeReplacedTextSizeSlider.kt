package com.example.readr.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.example.readr.Variables

@Composable
fun ChangeReplacedTextSizeSlider(onChange: (Int)->Unit = {}) {
    var sliderPosition by remember(Variables.overlayTextSize) { mutableFloatStateOf(Variables.overlayTextSize.toFloat()) }
    Column {

        Text(buildAnnotatedString {
            withStyle(SpanStyle(background = Color.White)) {
                append("Ovelay text size: $sliderPosition")
            }
        })

        Slider(
            value = sliderPosition,
            onValueChange = {
                val new = Math.round(it)
                Variables.overlayTextSize = new
                sliderPosition = new.toFloat()
                onChange(new)
                            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            steps = 25,
            valueRange = 15f..40f
        )
    }
}