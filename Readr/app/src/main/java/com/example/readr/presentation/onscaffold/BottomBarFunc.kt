package com.example.readr.presentation.onscaffold

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readr.MainActivity
import com.example.readr.openDyslexic
import com.example.readr.ui.theme.LocalTextSizes
import com.example.readr.ui.theme.LocalTextStyles

@Composable
fun BottomBar(idx:Int, setIdx:(Int)->Unit, tab_titles:List<String>, tab_images:List<Any>) {
    BottomAppBar(
        modifier = Modifier
            .heightIn(min=75.dp, max=75.dp)
            .fillMaxWidth()
            .onGloballyPositioned {
                val dm = MainActivity.context.resources.displayMetrics
                fun pxToDP(px:Int) : Int {
                    return Math.round(px/dm.density)
                }
                System.out.println("HEIGHT: ${pxToDP(it.size.height)}, WIDTH: ${pxToDP(it.size.width)}")
                                  },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.secondary,
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            verticalAlignment = Alignment.Top
        ) {
            TabRow(selectedTabIndex = idx) {
                tab_titles.forEachIndexed { indx, name ->
                    Tab(
                        selected = idx == indx,
                        onClick = { setIdx(indx) },
                        icon = {
                            if (tab_images[indx] is Int) {
                                Image(
                                    painterResource(tab_images[indx] as Int),
                                    name,
                                    modifier = Modifier.sizeIn(
                                        minWidth = 40.dp,
                                        maxWidth = 40.dp,
                                        minHeight = 20.dp,
                                        maxHeight = 20.dp
                                    ),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                                )
                            } else if (tab_images[indx] is ImageVector) {
                                Image(
                                    tab_images[indx] as ImageVector,
                                    name,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                                    modifier = Modifier.sizeIn(
                                        minWidth = 40.dp,
                                        maxWidth = 40.dp,
                                        minHeight = 20.dp,
                                        maxHeight = 20.dp
                                    )
                                )
                            }
                        },
                        text = {
                            Text(
                                name,
                                //style = LocalTextStyles.current.m,
                                style = TextStyle(fontSize = 16.sp, fontFamily= openDyslexic),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier.height(70.dp)
                    )
                }
            }
        }
    }
}

