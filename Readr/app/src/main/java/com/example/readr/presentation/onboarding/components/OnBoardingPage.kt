package com.example.readr.presentation.onboarding.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.readr.presentation.Dimens
import com.example.readr.presentation.onboarding.Page
import com.example.readr.ui.theme.LocalTextStyles

@Composable
fun OnBoardingPage(
    modifier: Modifier = Modifier,
    page: Page
) {

    Column(modifier = modifier) {
        if (page.image is Int) {
            Image(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f)
                , painter=painterResource(page.image as Int), contentDescription=null)
        } else if (page.image is ImageVector) {
            Image(page.image as ImageVector, null, modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f), contentScale= ContentScale.Crop)
        }

        /*Spacer(modifier = Modifier.height(Dimens.MediumPadding1))

        Text(text=page.title, modifier = Modifier.padding(horizontal = Dimens.MediumPadding2), style = LocalTextStyles.current.m, color= MaterialTheme.colorScheme.onPrimary)
        */
    }

}