package com.example.readr.presentation.onscaffold


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.readr.forceRecomposeWith
import com.example.readr.openDyslexic
import com.example.readr.ui.theme.LocalTextStyles

class DisplayTopBarFunc {
}
// NOT COLLAPSIBLE:

// with back button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayTopBar(title:String, backButtonFunc:(() -> Unit), dropdownItems:List<DDItem> = listOf() ) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Row(modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically, ) {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontSize=20.sp, fontFamily= openDyslexic)
                )

                if (dropdownItems.size > 0) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(COLLAPSED_TOP_BAR_HEIGHT),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        var showMenu by remember { mutableStateOf(false) }

                        IconButton(
                            onClick = {
                                showMenu = !showMenu
                            }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier,
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier,
                        ) {
                            for (ddItem in dropdownItems) ddItem.Show()
                        }
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                backButtonFunc()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = ""
                )
            }
        }
    )
}

// without back button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayTopBar(title:String, dropdownItems:List<DDItem> = listOf() ) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Row(modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically, ) {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontSize=20.sp, fontFamily= openDyslexic)
                )

                if (dropdownItems.size > 0) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(COLLAPSED_TOP_BAR_HEIGHT),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        var showMenu by remember { mutableStateOf(false) }

                        IconButton(
                            onClick = {
                                showMenu = !showMenu
                            }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier,
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier,
                        ) {
                            for (ddItem in dropdownItems) ddItem.Show()
                        }
                    }
                }
            }
        },
    )
}

