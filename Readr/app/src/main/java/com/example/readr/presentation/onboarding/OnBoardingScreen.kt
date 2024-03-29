package com.example.readr.presentation.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readr.R
import com.example.readr.presentation.Dimens
import com.example.readr.presentation.onboarding.components.OnBoardingButton
import com.example.readr.presentation.onboarding.components.OnBoardingPage
import com.example.readr.presentation.onboarding.components.OnBoardingTextButton
import com.example.readr.presentation.onboarding.components.PageIndicator
import com.example.readr.ui.theme.LocalTextStyles
import kotlinx.coroutines.launch


val pages = listOf<Page>(
    Page(
        title = "Readr",
        description = "Welcome to Readr! An application designed to assist in reading! (including Dyslexia, Presbyopia)",
        image = R.drawable.appicon,
    ),

    Page(
        title = "Dashboard",
        description = "Welcome to the Dashboard! View history, or open the accessibility page in Settings to easily find the accessibility menu for this service (explained later). You can also press the camera button to use the camera feature! ",
        image = R.drawable.dashboard,
    ),

    Page(
        title = "History",
        description = "Welcome to the Dashboard! View history, or open the accessibility page in Settings to easily find the accessibility menu for this service (explained later). You can also press the camera button to use the camera feature! ",
        image = R.drawable.history_page,
    ),

    Page(
        title = "Settings",
        description = "Here, you can change the size of text displayed in this app, or the size of text drawn by the accessibility menu or camera feature. ",
        image = R.drawable.settings,
    ),

    Page(
        title = "Camera (when using) ",
        description = "When you click the camera icon, the app will detect the text in the image and display it! Use the sliders at the bottom and right to adjust the position of text displayed, and the slider above to adjust the text size. Click the button below to capture that screen with the camera. ",
        image = R.drawable.camera_using,
    ),

    Page(
        title = "Camera (when done)",
        description = "After clicking the camera capture button, you will see this screen, where you can easily view the text, and choose from a list of actions to take from the bottom, which is scrollable. ",
        image = R.drawable.camera_done,
    ),

    Page(
        title = "Camera (copying dialog) ",
        description = "If you click the \"Select all\" button, you will see this dialog, and you can easily copy the text to your clipboard. ",
        image = R.drawable.camera_done,
    ),

    Page(
        title = "Reading",
        description = "If you choose to send to focused reading, or click the button, you may enter Focused Reading mode. The app will detect if you read the words and mark your progress, while you can use the help or skip buttons below if you are stuck. ",
        image = R.drawable.read_init,
    ),

    Page(
        title = "Reading (completed)",
        description = "After finishing reading the piece of text, you will be rewarded with confetti, claps, and an amazing quote! Tap anywhere to dismiss. ",
        image = R.drawable.read_complete,
    ),

    Page(
        title = "More functions",
        description = "There may also be other functions you can use if you press the three dots on the top right ;) (there's an amazing animation waiting for you :O)",
        image = R.drawable.more_actions,
    ),

    Page(
        title = "Accessibility Service (activation)",
        description = "If you recall the button on the dashboard, it takes you to this settings screen for you to enable the accessibility service for this application. ",
        image = R.drawable.amenu_settings_page_outer,
    ),

    Page(
        title = "Accessibility Service (shortcut)",
        description = "Enable the shortcut as shown, and you can see a floating accessibility button. This can be hidden at the edge of the screen (as shown in this image) ",
        image = R.drawable.amenu_settings_page_inner,
    ),

    Page(
        title = "Accessibility Service (usage)",
        description = "When you click the button, text will appear above your screen, using a very readable and cute font :) (OpenDyslexic). The big red X closes this, and lets you continue with whatever you were doing. ",
        image = R.drawable.amenu_usage,
    ),


)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(initPage:Int? = null, endPage:Int?=initPage, endFunc:()->Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        lateinit var pagerState:PagerState
        pagerState = if (initPage != null) {
            rememberPagerState(initPage!!) {
                pages.size
            }
        } else {
            rememberPagerState(0) {
                pages.size
            }
        }

        val buttonState = remember {
            derivedStateOf {
                when (pagerState.currentPage) {
                    endPage -> listOf("Back", "Return")
                    0 -> listOf("", "Next")
                    pages.size-1 -> listOf("Back", "Complete!")
                    else -> listOf("Back", "Next")
                }
            }
        }


        Spacer(modifier = Modifier.weight(0.5f))

        Text(pages[pagerState.currentPage].title, fontSize=20.sp, maxLines=1, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            style= LocalTextStyles.current.l)

        HorizontalPager(state = pagerState) {
            OnBoardingPage(page = pages[it])
        }

        Spacer(modifier = Modifier.weight(0.35f))

        Text(pages[pagerState.currentPage].description, fontSize=10.sp, maxLines=4, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            style= LocalTextStyles.current.m)

        Spacer(modifier = Modifier.weight(0.65f))

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.MediumPadding2)
            .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PageIndicator(modifier = Modifier.width(Dimens.IndicatorWidth), pageSize=pages.size, selectedPage = pagerState.currentPage)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val scope = rememberCoroutineScope()
                if (buttonState.value[0].isNotEmpty()) {
                    OnBoardingTextButton(text = buttonState.value[0]) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage-1)
                        }
                    }
                }

                OnBoardingButton(text = buttonState.value[1]) {
                    scope.launch {
                        if (pagerState.currentPage == pages.size-1 || pagerState.currentPage == endPage) {
                            endFunc()
                        } else {
                            pagerState.animateScrollToPage(pagerState.currentPage+1)
                        }
                    }
                }

            }

        }


        Spacer(modifier = Modifier.weight(0.5f))


    }
}


