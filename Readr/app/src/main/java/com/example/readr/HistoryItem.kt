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
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.readr.customcomposables.AdaptiveText
import com.example.readr.data.ImageLoader
import com.example.readr.ui.theme.LocalTextStyles
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HistoryItem(val num:Int) {

    companion object {
        val width = 160.dp
        val height = 160.dp
    }

    val imgl = ImageLoader()
    var init_bm: ImageBitmap? = null
    var final_bm: ImageBitmap? = null

    init {
        imgl.LoadImageBytes("image_${num}_init.png", { System.out.println("LOADING INIT $num") ; init_bm = it ; System.out.println("$init_bm") ; MainActivity.loadedHistItems++ })
        imgl.LoadImageBytes("image_${num}_final.png", { System.out.println("LOADING FINAL $num") ; final_bm = it ; System.out.println("$final_bm") ; MainActivity.loadedHistItems++ })
    }

    @Composable
    fun ShowView(showDetails:()->Unit, onFailure:()->Unit) {
        //if ((!(::init_bm.isInitialized)) or (!(::final_bm.isInitialized))) {
        if ((init_bm == null) or (final_bm == null)) {
            rememberCoroutineScope().launch {
                System.out.println("ERROR?")
                delay(1000)
                onFailure()
            }
            return
        }

        System.out.println("$num : #$init_bm and $final_bm")

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
                modifier = Modifier
                    .height(height)
                    .width(width)
                    .border(width = 4.dp, color = Color.Gray, shape = RoundedCornerShape(16.dp))
                    .clickable { showDetails() },
                contentAlignment = Alignment.Center
            ) {


                Image(init_bm!!, "", modifier = Modifier
                    .width(width)
                    .height(height)
                    .padding(12.dp),
                    contentScale = ContentScale.Crop)


                // can add label of time if possible yes
                /*Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(chapterNumStr, style=LocalTextStyles.current.l, modifier = Modifier.fillMaxWidth().wrapContentHeight(), textAlign = TextAlign.Center)
                }*/
                //System.out.println("YES BOX")
            }
        }
    }

    @Composable
    fun ShowDetails(onFailure: ()->Unit, goToMoveTextScreen:()->Unit ) {
        /*if ((init_bm == null) or (final_bm == null)) {
            rememberCoroutineScope().launch {
                System.out.println("ERROR?")
                delay(1000)
                onFailure()
            }
            return
        }*/

        //System.out.println("SHOWING DETAILS FOR NUM $num W")
        //System.out.println("MA LHA: ${MainActivity.loadedHistItems}")
        //System.out.println("$init_bm and $final_bm")
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            item {
                Text("SERVICE INPUT: ", style= LocalTextStyles.current.l)

                Spacer(Modifier.height(4.dp))

                Image(init_bm!!, "Image of screen before using service")
            }

            item {
                Divider()
            }


            item {
                Text("SERVICE OUTPUT: ", style= LocalTextStyles.current.l)

                Spacer(Modifier.height(4.dp))

                Image(final_bm!!, "Image of screen after using service")
            }

            item {
                Divider()
            }

            item {
                Button(goToMoveTextScreen, modifier = Modifier.padding(16.dp)) {
                    AdaptiveText("Enable moving text on output", LocalTextStyles.current.m,
                        maxLines=1, softWrap=false).ShowOverflowWidth()
                }
            }

        }
    }
}