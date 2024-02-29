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
import kotlinx.coroutines.launch


val pages = listOf<Page>(
    Page(
        title = "Toggle List Layout",
        description = "Toggle the list layout of the chapters for your own view!",
        image = R.drawable.ic_launcher_background,
    ),

    Page(
        title = "Module Information",
        description = "View the module information for your reference at a click!",
        image = R.drawable.ic_launcher_background,
    ),

    Page(
        title = "Dark Mode",
        description = "Switch to Dark Mode using the Overflow menu!",
        image = R.drawable.ic_launcher_background,
    )

)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(endFunc:()->Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState(0) {
            pages.size
        }

        val buttonState = remember {
            derivedStateOf {
                when (pagerState.currentPage) {
                    0 -> listOf("", "Next")
                    pages.size-1 -> listOf("Back", "Get Started")
                    else -> listOf("Back", "Next")
                }
            }
        }

        Text(pages[pagerState.currentPage].title, fontSize=20.sp, maxLines=1, modifier = Modifier.fillMaxWidth().padding(16.dp))

        HorizontalPager(state = pagerState) {
            OnBoardingPage(page = pages[it])
        }

        Spacer(modifier = Modifier.weight(0.5f))

        Text(pages[pagerState.currentPage].description, fontSize=10.sp, maxLines=4, modifier = Modifier.fillMaxWidth().padding(16.dp))

        Spacer(modifier = Modifier.weight(0.5f))

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.MediumPadding2)
            .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PageIndicator(modifier = Modifier.width(Dimens.IndicatorWidth), pageSize=pages.size, selectedPage = pagerState.currentPage)

            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        if (pagerState.currentPage == pages.size-1) {
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


