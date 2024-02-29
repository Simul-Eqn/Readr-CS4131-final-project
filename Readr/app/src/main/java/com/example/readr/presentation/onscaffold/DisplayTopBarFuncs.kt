package com.example.readr.presentation.onscaffold


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

class DisplayTopBarFunc {
}
// NOT COLLAPSIBLE:

// with back button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayTopBar(title:String, backButtonFunc:(() -> Unit)) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(title, maxLines=1, overflow = TextOverflow.Ellipsis)
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
fun DisplayTopBar(title:String) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(title, maxLines=1, overflow = TextOverflow.Ellipsis)
        },
    )
}

