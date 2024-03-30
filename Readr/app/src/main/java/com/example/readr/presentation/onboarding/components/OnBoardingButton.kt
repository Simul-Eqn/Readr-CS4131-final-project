package com.example.readr.presentation.onboarding.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.readr.ui.theme.LocalTextStyles


@Composable
fun OnBoardingButton(
    text:String,
    onClick:() -> Unit
) {
    Button(onClick=onClick, colors=ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(text, style = LocalTextStyles.current.s)
    }
}

@Composable
fun OnBoardingTextButton(
text:String,
onClick:() -> Unit
) {
    Button(onClick=onClick, colors=ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
    ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(text, style = LocalTextStyles.current.s)
    }
}

