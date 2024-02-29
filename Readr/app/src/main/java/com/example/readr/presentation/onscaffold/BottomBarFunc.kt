package com.example.readr.presentation.onscaffold

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.readr.ui.theme.LocalTextSizes

@Composable
fun BottomBar(idx:Int, setIdx:(Int)->Unit, tab_titles:List<String>, tab_images:List<Any>) {
    BottomAppBar(
        modifier = Modifier
            .height(75.dp)
            .fillMaxWidth(),
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
                                    )
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
                                fontSize = LocalTextSizes.current.m,
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
