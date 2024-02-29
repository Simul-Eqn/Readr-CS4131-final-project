package com.example.readr.presentation.onscaffold

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.readr.R
import com.example.readr.presentation.themeswitcher.ThemeSwitcher
import com.example.readr.ui.theme.LocalSpacings
import com.example.readr.ui.theme.LocalTextStyles

class DDItem(var itemText:@Composable()()->Unit, var onClick:()->Unit) {

    @Composable
    fun Show() {
        DropdownMenuItem(
            text = { itemText() },
            onClick = { onClick() }
        )
    }

}