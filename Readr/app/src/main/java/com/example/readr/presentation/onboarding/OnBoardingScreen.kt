package com.example.readr.presentation.onboarding

import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.readr.customcomposables.ForceShowBottomColumn
import com.example.readr.customcomposables.ForceShowRightRow
import com.example.readr.MainActivity
import com.example.readr.R
import com.example.readr.noRippleClickable
import com.example.readr.presentation.Dimens
import com.example.readr.presentation.onboarding.components.OnBoardingPage
import com.example.readr.presentation.onboarding.components.PageIndicator
import com.example.readr.ui.theme.LocalTextStyles
import kotlinx.coroutines.launch


val pages = listOf<Page>(
    Page(
        title = "Readr",
        description = "Welcome to Readr! An application designed to assist in reading! (including Dyslexia, Presbyopia)",
        light_image = R.drawable.appicon,
        dark_image = R.drawable.appicon,
    ),

    Page(
        title = "Dashboard",
        description = "Welcome to the Dashboard! View history, or open the accessibility page in Settings to easily find the accessibility menu for this service (explained later). You can also press the camera button to use the camera feature! ",
        light_image = R.drawable.dashboard_light,
        dark_image = R.drawable.dashboard_dark,
    ),

    Page(
        title = "History Page",
        description = "After clicking the history icon on the dashboard, you can view a page filled with history items! ",
        light_image = R.drawable.history_page_light,
        dark_image = R.drawable.history_page_dark,
    ),

    Page(
        title = "History Item",
        description = "In this screen, you can view your past uses of the app! If you accidentally closed, or just want to see something you were looking at before, you can click one of these! You can also adjust the position of the text. ",
        light_image = R.drawable.history_item_page_light,
        dark_image = R.drawable.history_item_page_dark,
    ),

    Page(
        title = "Settings",
        description = "In this screen, you can change the size of text displayed in this app, or the size of text drawn by the accessibility menu or camera feature. ",
        light_image = R.drawable.settings_light,
        dark_image = R.drawable.settings_dark,
    ),

    Page(
        title = "Camera (when using) ",
        description = "When you click the camera icon, the app will detect the text in the image and display it! Use the sliders at the bottom and right to adjust the position of text displayed, and the slider above to adjust the text size. Click the button below to capture that screen with the camera. ",
        light_image = R.drawable.camera_using_light,
        dark_image = R.drawable.camera_using_dark,
    ),

    Page(
        title = "Camera (when done)",
        description = "After clicking the camera capture button, you will see this screen, where you can easily view the text, and choose from a list of actions to take from the bottom, which is scrollable. ",
        light_image = R.drawable.camera_done_light,
        dark_image = R.drawable.camera_done_dark,
    ),

    Page(
        title = "Camera (copying dialog) ",
        description = "If you click the \"Select all\" button, you will see this dialog, and you can easily copy the text to your clipboard. (it is scrollable)",
        light_image = R.drawable.camera_copy_light,
        dark_image = R.drawable.camera_copy_dark,
    ),

    Page(
        title = "Gallery",
        description = "If you click the button at the bottom left of the camera, will get a dialog to choose an image, and will get to this screen. ",
        light_image = R.drawable.gallery_light,
        dark_image = R.drawable.gallery_dark,
    ),

    Page(
        title = "Reading",
        description = "If you choose to send to focused reading, or click the button, you may enter Focused Reading mode. The app will detect if you read the words and mark your progress, while you can use the help or skip buttons below if you are stuck. ",
        light_image = R.drawable.read_init_light,
        dark_image = R.drawable.read_init_dark,
    ),

    Page(
        title = "Reading (completed)",
        description = "After finishing reading the piece of text, you will be rewarded with confetti, claps, and an amazing quote! Tap anywhere to dismiss. ",
        light_image = R.drawable.read_complete_light,
        dark_image = R.drawable.read_complete_dark,
    ),

    Page(
        title = "More functions",
        description = "There may also be other functions you can use if you press the three dots on the top right ;) (there's an amazing animation waiting for you :O) You may return to this page through this menu :) ",
        light_image = R.drawable.more_actions_light,
        dark_image = R.drawable.more_actions_dark,
    ),

    Page(
        title = "Accessibility Service",
        description = "If you recall the button on the dashboard, it takes you to this settings screen for you to enable the accessibility service for this application. ",
        light_image = R.drawable.amenu_settings_page_outer_light,
        dark_image = R.drawable.amenu_settings_page_outer_dark,
    ),

    Page(
        title = "Accessibility Shortcut",
        description = "Enable the shortcut as shown, and you can see a floating accessibility button. Clicking this will active the accessibility service on your screen. ",
        light_image = R.drawable.amenu_settings_page_inner_light,
        dark_image = R.drawable.amenu_settings_page_inner_dark,
    ),

    Page(
        title = "Accessibility Service Usage",
        description = "When you click the button, text will appear above your screen, using a very readable and cute font :) (OpenDyslexic). The big red X closes this, and lets you continue with whatever you were doing. (the X is also hidden in the history record!)",
        light_image = R.drawable.amenu_usage_light,
        dark_image = R.drawable.amenu_usage_dark,
    ),


)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(initPage:Int? = null, endPage:Int?=initPage, darkTheme:Boolean, endFunc:()->Unit) {
    //val initScale = Variables.textScale
    //LocalTextStyles.current.setTextScale(1.0f)


    System.out.println("ONBOARDING: $initPage and $endPage")

    Column(modifier = Modifier
        .fillMaxSize()
        .noRippleClickable {
            MainActivity.window.decorView.apply {
                systemUiVisibility =
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
            }; System.out.println("HIDING NAVBAR")
        }
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
        //Spacer(Modifier.height(32.dp))


        var redrawNum by remember { mutableStateOf(0) }



        key(redrawNum) {

            val titleTextStyleInit = LocalTextStyles.current.l
            var titleTextStyle by remember { mutableStateOf(titleTextStyleInit) }
            var displayTitle by remember { mutableStateOf(false) }


            var titleHeight by remember { mutableStateOf(100f) }

            ForceShowRightRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp),
                //horizontalArrangement = Arrangement.SpaceBetween,
                //verticalAlignment = Alignment.CenterVertically,
                spacingDP = 32,
                drawLastAtRight = true,
            ) {

                Box(
                    modifier = Modifier.padding(vertical=16.dp)
                ) {
                    Text(
                        pages[pagerState.currentPage].title,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .wrapContentSize()
                            .drawWithContent { if (displayTitle) drawContent() },
                        color = if (darkTheme) Color.White else Color.Black,
                        style = titleTextStyle,
                        onTextLayout = {
                            titleHeight = MainActivity.pxToDP(it.size.height)
                            if (it.didOverflowWidth) {
                                titleTextStyle =
                                    titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                            } else {
                                displayTitle = true
                            }
                        }
                    )
                }

                //System.out.println("DISPLAYEDO NBOARDING TITLE TEXT, ${LocalTextStyles.current.l}")


                TextButton(
                    onClick = {
                        endFunc()
                    },
                    modifier = Modifier.wrapContentSize(),
                    contentPadding = PaddingValues(vertical=16.dp)
                ) {
                    Row(
                        //modifier = Modifier.padding(vertical=16.dp),
                        modifier = Modifier.wrapContentSize(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painterResource(R.drawable.skip_icon),
                            "Skip help page button",
                            modifier = Modifier.height(titleHeight.dp)
                        )
                        //Text("SKIP", maxLines=1, softWrap=false, style= LocalTextStyles.current.l)

                        Text(
                            "SKIP",
                            maxLines = 1,
                            softWrap = false,
                            style = titleTextStyle,
                            modifier = Modifier
                                .wrapContentSize()
                                .drawWithContent { if (displayTitle) drawContent() },
                            onTextLayout = {
                                titleHeight = MainActivity.pxToDP(it.size.height)
                                if (it.didOverflowWidth) {
                                    titleTextStyle =
                                        titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                                } else {
                                    displayTitle = true
                                }
                            }
                        )

                    }
                }

            }

        }



        Spacer(modifier = Modifier.weight(0.65f))

        //var prevPage by remember { mutableStateOf( initPage ) }
        val isDragged = pagerState.interactionSource.collectIsDraggedAsState()
        var shldLaunch by remember { mutableStateOf(false) }
        //val redrawScope = rememberCoroutineScope()

        HorizontalPager(state = pagerState) {
            OnBoardingPage(page = pages[it], lightMode = !darkTheme)
            if (isDragged.value) {
                shldLaunch = true
            } else if (shldLaunch) {
                redrawNum++
                shldLaunch = false
            }
        }

        Spacer(modifier = Modifier.weight(0.35f))



        ForceShowBottomColumn(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            spacingDP=20,
            drawLastAtBottom = true,
        ) {


            key(redrawNum) {

                val contentTextStyleInit = LocalTextStyles.current.m
                var contentTextStyle by remember { mutableStateOf(contentTextStyleInit) }
                var displayContent by remember { mutableStateOf(false) }

                Text(pages[pagerState.currentPage].description,
                    maxLines = 6,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .drawWithContent { if (displayContent) drawContent() },
                    style = contentTextStyle,
                    color = if (darkTheme) Color.White else Color.Black,
                    onTextLayout = {
                        if (it.didOverflowHeight) {
                            contentTextStyle =
                                contentTextStyle.copy(fontSize = contentTextStyle.fontSize * 0.9)
                        } else {
                            displayContent = true
                        }
                    })

            }

            //System.out.println("DISPLAYED ONBOARDING DESC ${LocalTextStyles.current.m}")

            // Spacer(modifier = Modifier.weight(0.65f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.MediumPadding2)
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PageIndicator(
                    modifier = Modifier.width(Dimens.IndicatorWidth),
                    pageSize = pages.size,
                    selectedPage = pagerState.currentPage
                )

                Spacer(modifier = Modifier.width(16.dp))

                key(redrawNum) {

                    val buttonTextStyleInit = LocalTextStyles.current.l
                    var buttonTextStyle by remember { mutableStateOf(buttonTextStyleInit) }
                    var displayButtons by remember { mutableStateOf(false) }


                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val scope = rememberCoroutineScope()


                        if (buttonState.value[0].isNotEmpty()) {

                            Button(
                                onClick = {
                                    scope.launch {

                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)

                                        redrawNum++

                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary,
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal=6.dp),
                            ) {

                                Text(buttonState.value[0],
                                    maxLines = 1,
                                    softWrap=false,
                                    modifier = Modifier
                                        .drawWithContent { if (displayButtons) drawContent() },
                                    style = buttonTextStyle,
                                    onTextLayout = {
                                        if (it.didOverflowWidth) {
                                            buttonTextStyle =
                                                buttonTextStyle.copy(fontSize = buttonTextStyle.fontSize * 0.9)
                                        } else {
                                            displayButtons = true
                                        }
                                    })

                            }

                        }


                        Button(
                            onClick = {
                                scope.launch {
                                    if (pagerState.currentPage == pages.size - 1 || pagerState.currentPage == endPage) {
                                        endFunc()
                                    } else {

                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)

                                        redrawNum++

                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal=6.dp),
                        ) {
                            //Text(buttonState.value[1], style = LocalTextStyles.current.s)

                            Text(buttonState.value[1],
                                maxLines = 1,
                                softWrap=false,
                                modifier = Modifier
                                    .drawWithContent { if (displayButtons) drawContent() },
                                style = buttonTextStyle,
                                onTextLayout = {
                                    if (it.didOverflowWidth) {
                                        buttonTextStyle =
                                            buttonTextStyle.copy(fontSize = buttonTextStyle.fontSize * 0.9)
                                    } else {
                                        displayButtons = true
                                    }
                                })
                        }

                    }

                }


            }


        }


        //Spacer(modifier = Modifier.weight(0.5f))
        //Spacer(modifier = Modifier.height(32.dp))


    }
}



