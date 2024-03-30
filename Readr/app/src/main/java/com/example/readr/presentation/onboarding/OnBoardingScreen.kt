package com.example.readr.presentation.onboarding

import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.readr.MainActivity
import com.example.readr.R
import com.example.readr.Variables
import com.example.readr.noRippleClickable
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
        description = "Here, you can view your past uses of the app! If you accidentally closed, or just want to see something you were looking at before, you can click one of these! ",
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
        image = R.drawable.camera_copy,
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
        title = "Accessibility Service",
        description = "If you recall the button on the dashboard, it takes you to this settings screen for you to enable the accessibility service for this application. ",
        image = R.drawable.amenu_settings_page_outer,
    ),

    Page(
        title = "Accessibility Shortcut",
        description = "Enable the shortcut as shown, and you can see a floating accessibility button. This can be hidden at the edge of the screen (as shown in this image) ",
        image = R.drawable.amenu_settings_page_inner,
    ),

    Page(
        title = "Accessibility Service Usage",
        description = "When you click the button, text will appear above your screen, using a very readable and cute font :) (OpenDyslexic). The big red X closes this, and lets you continue with whatever you were doing. ",
        image = R.drawable.amenu_usage,
    ),


)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(initPage:Int? = null, endPage:Int?=initPage, endFunc:()->Unit) {
    //val initScale = Variables.textScale
    LocalTextStyles.current.setTextScale(1.0f)

    Column(modifier = Modifier
        .fillMaxSize()
        .noRippleClickable { MainActivity.window.decorView.apply { systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN } ; System.out.println("HIDING NAVBAR")}
    ) {
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

        //Spacer(modifier = Modifier.weight(0.5f))
        Spacer(Modifier.height(32.dp))

        val titleTextStyleInit = LocalTextStyles.current.l
        var titleTextStyle by remember { mutableStateOf(titleTextStyleInit) }
        var displayTitle by remember { mutableStateOf(false) }

        Row(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically, ) {

            Text(
                pages[pagerState.currentPage].title,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(16.dp).drawWithContent{ if (displayTitle) drawContent() },
                style= titleTextStyle,
                onTextLayout = {
                    if (it.didOverflowWidth) {
                        titleTextStyle = titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                    } else {
                        displayTitle = true
                    }
                }
            )

            //System.out.println("DISPLAYEDO NBOARDING TITLE TEXT, ${LocalTextStyles.current.l}")


            TextButton(
                onClick = {
                    endFunc()
                }, modifier = Modifier.wrapContentSize(), ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.skip_icon), "Skip word button", modifier=Modifier.heightIn(20.dp, 20.dp))
                    Text("SKIP", maxLines=1, softWrap=false, style= LocalTextStyles.current.l)
                }
            }

        }

        Spacer(modifier = Modifier.weight(0.65f))

        HorizontalPager(state = pagerState) {
            OnBoardingPage(page = pages[it])
        }

        Spacer(modifier = Modifier.weight(0.35f))


        val contentTextStyleInit = LocalTextStyles.current.m
        var contentTextStyle by remember { mutableStateOf(contentTextStyleInit) }
        var displayContent by remember { mutableStateOf(false) }

        Text(pages[pagerState.currentPage].description,
            maxLines=6,
            overflow = TextOverflow.Clip,
            modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .drawWithContent{ if (displayContent) drawContent() },
            style= contentTextStyle,
            onTextLayout = {
                if (it.didOverflowHeight) {
                    contentTextStyle = contentTextStyle.copy(fontSize = contentTextStyle.fontSize * 0.9)
                } else {
                    displayContent = true
                }
            })

        //System.out.println("DISPLAYED ONBOARDING DESC ${LocalTextStyles.current.m}")

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

                            displayContent = false
                            contentTextStyle = contentTextStyleInit

                            displayTitle = false
                            titleTextStyle = titleTextStyleInit

                        }
                    }
                }

            }

        }


        //Spacer(modifier = Modifier.weight(0.5f))
        Spacer(modifier = Modifier.height(32.dp))


    }
}


