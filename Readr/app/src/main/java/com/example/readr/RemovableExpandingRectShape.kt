// made into my own custom shape

package com.example.readr

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class RemovableExpandingRectShape(private val offsetX: Float, private val offsetY: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            path = Path().apply {

                reset()

                /*lineTo(x = 0f, y = 0f)
                lineTo(x = size.width-offsetX, y = 0f)
                lineTo(x = size.width-offsetX, y = offsetY)
                lineTo(x = size.width, y = offsetY)
                lineTo(x = size.width, y = size.height)
                lineTo(x = 0f, y = size.height)*/

                lineTo(x = 0f, y = 0f)
                lineTo(x = size.width-offsetX+15.0f, y = 0f)
                lineTo(x = size.width-offsetX, y = 15.0f)
                lineTo(x = size.width-offsetX, y = offsetY-15.0f)
                lineTo(x = size.width-offsetX+15.0f, y = offsetY)
                lineTo(x = size.width-15.0f, y = offsetY)
                lineTo(x = size.width, y = offsetY-15.0f)
                lineTo(x = size.width, y = size.height)
                lineTo(x = 0f, y = size.height)

                close()

            }
        )
    }

}

// this shape is the shape of the original screen, e.g. light->dark, this is the shape of light mode to display.



