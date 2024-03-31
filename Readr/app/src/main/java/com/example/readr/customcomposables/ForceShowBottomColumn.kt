package com.example.readr.customcomposables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints

@Composable
fun ForceShowBottomColumn(modifier: Modifier = Modifier, spacingDP:Int=0,
                          prioritizeBottomWidth:Boolean=true, drawLastAtBottom:Boolean=false, useMaxHeight: Boolean =true, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        check(measurables.size == 2) { "This composable requires 2 children only" }
        val first = measurables.first()
        val last = measurables.last()

        val looseConstraints = constraints.copy(minWidth=0, minHeight=0)
        val lastMeasurable = last.measure(looseConstraints)
        val availableHeight = constraints.maxHeight - lastMeasurable.height - spacingDP // available height for first child, since we force last child to take priority

        var maxWidth = 0
        var maxHeight = 0

        if (prioritizeBottomWidth) {
            maxWidth = lastMeasurable.width
            maxHeight = first.maxIntrinsicHeight(maxWidth).coerceAtMost(availableHeight)
        } else {
            maxWidth = first.maxIntrinsicWidth(availableHeight)
            maxHeight = first.minIntrinsicHeight(maxWidth)
        }


        val firstMeasurable = first.measure(
            Constraints(
                minWidth = 0,
                maxWidth = maxWidth,
                minHeight = maxHeight,
                maxHeight = maxHeight,
            )
        )
        layout(
            maxWidth,
            if (useMaxHeight) constraints.maxHeight else (maxHeight+spacingDP+lastMeasurable.height),
        ) {
            firstMeasurable.place(0,0)
            if (drawLastAtBottom && useMaxHeight) lastMeasurable.place(0, constraints.maxHeight - lastMeasurable.height)
            else lastMeasurable.place(0, maxHeight+spacingDP)
        }
    }
}