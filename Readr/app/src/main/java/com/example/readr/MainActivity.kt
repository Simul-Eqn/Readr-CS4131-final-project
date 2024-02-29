package com.example.readr

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.example.readr.presentation.onboarding.OnBoardingScreen
import com.example.readr.data.PersistentStorage
import com.example.readr.presentation.onscaffold.BottomBar
import com.example.readr.presentation.onscaffold.DDItem
import com.example.readr.presentation.onscaffold.ExpandedTopBar
import com.example.readr.presentation.onscaffold.WrapInColllapsedTopBar
import com.example.readr.presentation.themeswitcher.ThemeSwitcher
import com.example.readr.ui.theme.LocalSpacings
import com.example.readr.ui.theme.LocalTextStyles
import com.example.readr.ui.theme.ReadrTheme

class MainActivity : ComponentActivity() {

    val tab_titles:MutableList<String> = mutableListOf()
    val tab_images:MutableList<Any> = mutableListOf()
    var topBarImgs:MutableList<Any> = mutableListOf()

    var dropdownItems:MutableList<DDItem> = mutableListOf()

    var outerNavPageNo:Int = 0
    var innerNavTabNo:Int = 1

    fun initOnScaffolds() {

        // tabs and top bar images
        tab_titles.add("Reading")
        tab_images.add(Icons.Filled.Book)
        topBarImgs.add(R.drawable.ic_launcher_background)

        tab_titles.add("Dashboard")
        tab_images.add(R.drawable.home_icon)
        topBarImgs.add(R.drawable.ic_launcher_background)

        tab_titles.add("Settings")
        tab_images.add(R.drawable.settings_icon)
        topBarImgs.add(R.drawable.ic_launcher_background)


        // dropdown items


    }

    var darkTheme:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initOnScaffolds()

        setContent {

            val cursor = contentResolver.query(Uri.parse(PersistentStorage.URL), null, null, null, null)
            var temp = false
            if (cursor!!.moveToFirst()) temp = true
            cursor.close()
            var finishedOnboarding by remember { mutableStateOf(temp) }

            var localDarkTheme by remember { mutableStateOf(false) }
            var viewNo by remember(outerNavPageNo) { mutableIntStateOf(outerNavPageNo) }

            // add theme switcher
            dropdownItems.add(DDItem(
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.spacedBy(LocalSpacings.current.m),
                    ) {
                        Text("Theme: ", style= LocalTextStyles.current.m)
                        ThemeSwitcher(darkTheme = localDarkTheme, onClick = { darkTheme = !darkTheme; localDarkTheme=darkTheme })
                    }
                },
                {},
            ))

            ReadrTheme(darkTheme = darkTheme) {

                when (finishedOnboarding) {
                    true -> ShowView(viewNo)

                    false -> OnBoardingScreen {
                        finishedOnboarding = true
                        Log.w("ONBOARDING", "FINISHED: $finishedOnboarding")
                        val values = ContentValues()
                        values.put(PersistentStorage.rdm, "HEHEH DONE")
                        contentResolver.insert(PersistentStorage.CONTENT_URI, values)
                    }
                }

            }

        }
    }

    @Composable
    fun ShowView(viewNo:Int) {

        var idx by remember { mutableIntStateOf(innerNavTabNo) }
        fun setIdx(newIdx:Int) {
            idx = newIdx
            innerNavTabNo = newIdx
        }
        val topBarTitle by remember(idx) { mutableStateOf(tab_titles[idx]) }
        val topBarImg by remember(idx) { mutableStateOf(topBarImgs[idx]) }

        when (viewNo) {
            0 -> Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = { BottomBar( idx, { setIdx(it) } , tab_titles , tab_images ) },
                floatingActionButton = {
                    if (idx==1) FloatingActionButton( onClick = { outerNavPageNo = 1 }, containerColor=MaterialTheme.colorScheme.secondary )
                    { Image(painterResource(R.drawable.camera_icon), "Camera button") } },
            ) {
                val listState = rememberLazyListState()
                WrapInColllapsedTopBar(it, listState, dropdownItems, topBarTitle, idx!=1) {

                    var toggle by remember { mutableStateOf(false) }

                    LazyColumn(
                        state = listState,
                    ) {
                        if (outerNavPageNo == 0 && innerNavTabNo == 1) {
                            item() {
                                ExpandedTopBar(topBarImg, topBarTitle)
                            }
                        }

                        items(1) {
                            when (idx) {
                                0 -> ShowReadingView(toggle, { toggle = !toggle })
                                1 -> ShowDashboard(toggle, { toggle = !toggle })
                                2 -> ShowSettings(toggle, { toggle = !toggle })
                            }
                        }

                    }
                }
            }
        }
    }

    @Composable
    fun ShowReadingView(recomposeBool:Boolean, recompose:()->Unit) {
        Text("READING VIEW")
        //TODO
    }

    @Composable
    fun ShowDashboard(recomposeBool:Boolean, recompose:()->Unit) {
        Text("DASHBOARD VIEW")
        //TODO
    }

    @Composable
    fun ShowSettings(recomposeBool:Boolean, recompose:()->Unit) {
        Text("SETTINGS VIEW")
        //TODO
    }


}

