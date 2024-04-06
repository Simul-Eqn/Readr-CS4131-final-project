package com.example.readr.customcomposables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.readr.Variables
import com.example.readr.forceRecomposeWith
import com.example.readr.noRippleClickable
import com.example.readr.openDyslexic
import com.example.readr.ui.theme.LocalTextStyles
import kotlin.math.roundToInt

@Composable
fun ChangeReplacedTextSizeSlider(overlayTextSize:Float,
                                 textScale:Float= Variables.textScale, // this is to allow recomposition
                                 extra: Int=0,
                                 setOverlayTextSize:(Float)->Unit, ) {

    var ots by remember { mutableFloatStateOf(overlayTextSize) }

    Column(modifier = Modifier
        .padding(horizontal = 16.dp)
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
                modifier = Modifier
                    .forceRecomposeWith(textScale)
                    .forceRecomposeWith(overlayTextSize)
                    .forceRecomposeWith(extra)
                    .wrapContentWidth()
                    .drawWithContent { if (displayTitle) drawContent() },
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
        modifier = Modifier
            .forceRecomposeWith(textScale)
            .forceRecomposeWith(extra)
            .padding(horizontal = 16.dp)
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
                modifier = Modifier
                    .forceRecomposeWith(textScale)
                    .forceRecomposeWith(extra)
                    .wrapContentWidth()
                    .drawWithContent { if (displayTitle) drawContent() },
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

@Composable
fun ChangeFontFamilyItem(currFont:Int, setFont:(Int)->Unit, extra:Int, addToExtra:()->Unit) {
    key(extra) {
        var ddmenuOpen by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.wrapContentSize().padding(horizontal=16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            val initStyle = LocalTextStyles.current.l
            var titleTextStyle by remember { mutableStateOf(initStyle) }
            var displayTitle by remember { mutableStateOf(false) }


            Text(
                "Font Family: ",
                modifier = Modifier
                    .forceRecomposeWith(extra)
                    .wrapContentWidth()
                    .drawWithContent { if (displayTitle) drawContent() },
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

            Box(
                modifier = Modifier.wrapContentSize()
                    .border(BorderStroke(2.dp, Color.Gray), RoundedCornerShape(2.dp))
                    .padding(6.dp)
                    .clickable {
                        ddmenuOpen = true
                    },
                contentAlignment = Alignment.CenterEnd
            ) {

                Row(modifier = Modifier.wrapContentSize(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {

                    Text(
                        when (currFont) {
                            0 -> "OpenDyslexic"
                            1 -> "Sans-Serif"
                            else -> "Unknown"
                        },
                        modifier = Modifier
                            .forceRecomposeWith(extra)
                            .wrapContentWidth()
                            .drawWithContent { if (displayTitle) drawContent() },
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

                    Box(modifier = Modifier.wrapContentSize() ) {
                        /*Icon(Icons.Filled.ArrowDropDown, "Dropdown", modifier = Modifier.noRippleClickable {
                            ddmenuOpen = true
                        })*/
                        DropdownMenu(
                            expanded = ddmenuOpen,
                            onDismissRequest = { ddmenuOpen = false }) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "OpenDyslexic",
                                        modifier = Modifier
                                            .forceRecomposeWith(extra)
                                            .wrapContentWidth()
                                            .drawWithContent { if (displayTitle) drawContent() },
                                        style = titleTextStyle,
                                        fontFamily = openDyslexic,
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
                                },
                                onClick = {
                                    setFont(0) ; addToExtra()
                                })


                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Sans-Serif",
                                        modifier = Modifier
                                            .forceRecomposeWith(extra)
                                            .wrapContentWidth()
                                            .drawWithContent { if (displayTitle) drawContent() },
                                        style = titleTextStyle,
                                        fontFamily=FontFamily.SansSerif,
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
                                },
                                onClick = {
                                    setFont(1) ;  addToExtra()
                                })
                        }
                    }

                }

            }
        }
    }
}


