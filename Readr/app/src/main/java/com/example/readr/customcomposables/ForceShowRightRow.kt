package com.example.readr.customcomposables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints

@Composable
fun ForceShowRightRow(modifier: Modifier = Modifier, spacingDP:Int=0,
                      prioritizeRightHeight:Boolean=true, drawLastAtRight:Boolean = false, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        check(measurables.size == 2) { "This composable requires 2 children only" }
        val first = measurables.first()
        val last = measurables.last()

        val looseConstraints = constraints.copy(minWidth=0, minHeight=0)
        val lastMeasurable = last.measure(looseConstraints)

        val availableWidth = constraints.maxWidth - lastMeasurable.width - spacingDP // available height for first child, since we force last child to take priority

        var maxHeight=0
        var maxWidth=0

        if (prioritizeRightHeight) {
            maxHeight = lastMeasurable.height
            maxWidth = first.maxIntrinsicWidth(maxHeight).coerceAtMost(availableWidth)
        } else {
            maxHeight = first.maxIntrinsicHeight(availableWidth)
            maxWidth = first.minIntrinsicWidth(maxHeight)
        }

        val firstMeasurable = first.measure(
            Constraints(
                minWidth = maxWidth,
                maxWidth = maxWidth,
                minHeight = 0,
                maxHeight = maxHeight,
            )
        )
        layout(
            constraints.maxWidth,
            maxHeight,
        ) {
            firstMeasurable.place(0,0)
            if (!drawLastAtRight) lastMeasurable.place(maxWidth+spacingDP, 0)
            else lastMeasurable.place(constraints.maxWidth-lastMeasurable.width, 0)
        }
    }
}