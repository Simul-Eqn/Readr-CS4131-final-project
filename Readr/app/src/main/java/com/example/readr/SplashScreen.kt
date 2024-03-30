package com.example.readr

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@Composable
fun SplashScreen() {

    var alpha = remember {
        androidx.compose.animation.core.Animatable(0f)
    }

    var rot = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(true) {
        alpha.animateTo(1f, tween(1000, easing=LinearOutSlowInEasing))
        while (true) {
            rot.animateTo(360f, tween(1500, easing= LinearOutSlowInEasing))
            rot.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Color(0xF3ACE1AF)
        )
        ,
        contentAlignment = Alignment.Center,

    ) {
        Image(
            painterResource(R.drawable.appicon), "Application icon",
            modifier = Modifier.alpha(alpha.value).rotate(rot.value)
        )
    }
}