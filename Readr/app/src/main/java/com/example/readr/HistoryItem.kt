package com.example.readr

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readr.data.ImageLoader
import com.example.readr.ui.theme.LocalTextStyles

class HistoryItem(val num:Int) {

    companion object {
        val width = 150.dp
        val height = 150.dp
    }

    private val imgl = ImageLoader()

    @Composable
    fun ShowView(showDetails:()->Unit) {
        Box(
            modifier = Modifier.sizeIn(
                minWidth = width,
                maxWidth = width,
                minHeight = height,
                maxHeight = height,
            ),
            contentAlignment = Alignment.Center
        ) {

            Box(
                modifier = Modifier.height(height).width(width)
                    .border(width=4.dp, color= Color.Gray, shape = RoundedCornerShape(16.dp))
                    .clickable { showDetails() },
                contentAlignment = Alignment.Center
            ) {

                var bm by remember { mutableStateOf(ImageBitmap(100,100)) }
                imgl.LoadImageBytes("image_${num}_init.png", { bm = it })

                Image(bm, "", modifier = Modifier.width(width).height(height).padding(12.dp), colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.onBackground), contentScale = ContentScale.Crop)


                // can add label of time if possible yes
                /*Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(chapterNumStr, style=LocalTextStyles.current.l, modifier = Modifier.fillMaxWidth().wrapContentHeight(), textAlign = TextAlign.Center)
                }*/
                System.out.println("YES BOX")
            }
        }
    }

    @Composable
    fun ShowDetails() {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            item {
                Text("BEFORE USING SERVICE: ")

                Spacer(Modifier.height(4.dp))

                var bm1 by remember { mutableStateOf(ImageBitmap(100, 100)) }
                imgl.LoadImageBytes("image_${num}_init.png", { bm1 = it })
                Image(bm1, "Image of screen before using service")
            }

            item {
                Divider()
            }


            item {
                Text("AFTER USING SERVICE: ")

                Spacer(Modifier.height(4.dp))

                var bm2 by remember { mutableStateOf(ImageBitmap(100, 100)) }
                imgl.LoadImageBytes("image_${num}_final.png", { bm2 = it })
                Image(bm2, "Image of screen after using service")
            }

        }
    }
}