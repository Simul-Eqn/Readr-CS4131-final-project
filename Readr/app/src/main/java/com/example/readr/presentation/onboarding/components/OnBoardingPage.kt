package com.example.readr.presentation.onboarding.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.readr.presentation.onboarding.Page

@Composable
fun OnBoardingPage(
    modifier: Modifier = Modifier,
    page: Page,
    lightMode:Boolean=true,
) {

    Column(modifier = modifier) {
        if (lightMode || (page.dark_image == null)) {
            if (page.light_image is Int) {
                Image(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                    painter = painterResource(page.light_image as Int),
                    contentDescription = null
                )
            } else if (page.light_image is ImageVector) {
                Image(
                    page.light_image as ImageVector,
                    null,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            // dark mode images
            if (page.dark_image is Int) {
                Image(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                    painter = painterResource(page.dark_image as Int),
                    contentDescription = null
                )
            } else if (page.dark_image is ImageVector) {
                Image(
                    page.dark_image as ImageVector,
                    null,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f),
                    contentScale = ContentScale.Crop
                )
            }
        }

        /*Spacer(modifier = Modifier.height(Dimens.MediumPadding1))

        Text(text=page.title, modifier = Modifier.padding(horizontal = Dimens.MediumPadding2), style = LocalTextStyles.current.m, color= MaterialTheme.colorScheme.onPrimary)
        */
    }

}