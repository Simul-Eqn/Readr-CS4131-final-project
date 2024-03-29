package com.example.readr.presentation.onscaffold


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.readr.Variables
import com.example.readr.forceRecomposeWith
import com.example.readr.ui.theme.LocalSpacings
import com.example.readr.ui.theme.LocalTextStyles

class CollapsingTopBarFuncs {
}

val COLLAPSED_TOP_BAR_HEIGHT = 60.dp
val EXPANDED_TOP_BAR_HEIGHT = 300.dp

@Composable
fun ExpandedTopBar(imgRes: Any, title:String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxWidth()
            .height(EXPANDED_TOP_BAR_HEIGHT)
            .padding(LocalSpacings.current.xl),
        contentAlignment = Alignment.BottomStart
    ) {


        if (imgRes is Int) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(imgRes as Int),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        } else if (imgRes is ImageVector) {
            Image(
                imgRes,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }



        Text(
            modifier = Modifier,
            text = title,
            color = MaterialTheme.colorScheme.primary,
            style = LocalTextStyles.current.l,
        )
    }
}

@Composable
fun CollapsedTopBar(
    modifier: Modifier = Modifier,
    isCollapsed: Boolean,
    title:String,
    textScale: Float = Variables.textScale,
    MenuOptions:@Composable() (() -> Unit),
) {
    val color: Color by animateColorAsState(
        if (isCollapsed) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    )
    Box(
        modifier = modifier
            .background(color)
            .fillMaxWidth()
            .height(COLLAPSED_TOP_BAR_HEIGHT)
            .padding(16.dp)
            .forceRecomposeWith(textScale),
        contentAlignment = Alignment.BottomEnd,
    ) {
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomStart).forceRecomposeWith(textScale),
            visible = isCollapsed
        ) {
            Text(text = title, color = MaterialTheme.colorScheme.primary, style = LocalTextStyles.current.l,
                modifier = Modifier.forceRecomposeWith(textScale))
        }


        // overflow menu - visible no matter what :)

        Box(
            modifier = Modifier.align(Alignment.BottomEnd).forceRecomposeWith(textScale),
        ) {
            var showMenu by remember { mutableStateOf(false) }

            IconButton(
                onClick = {
                    showMenu = !showMenu
                }) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.forceRecomposeWith(textScale),
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.forceRecomposeWith(textScale),
            ) {
                MenuOptions()
            }
        }


    }
}


@Composable
fun WrapInColllapsedTopBar(padding: PaddingValues,
                           listState: LazyListState,
                           dropdownItems: List<DDItem>,
                           topBarTitle: String,
                           showAnyways: Boolean,
                           textScale: Float = Variables.textScale, // for forcing recompose
                           content:@Composable()()->Unit) {

    val overlapHeightPx = with(LocalDensity.current) {
        EXPANDED_TOP_BAR_HEIGHT.toPx() - COLLAPSED_TOP_BAR_HEIGHT.toPx()
    }
    val isCollapsed : Boolean by remember {
        derivedStateOf {
            val isFirstItemHidden =
                listState.firstVisibleItemScrollOffset > overlapHeightPx
            isFirstItemHidden || listState.firstVisibleItemIndex > 0
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        color = MaterialTheme.colorScheme.background
    ) {

        Box() {
            CollapsedTopBar(modifier = Modifier.zIndex(2f), isCollapsed=(isCollapsed || showAnyways), topBarTitle, textScale) {
                for (ddItem in dropdownItems) ddItem.Show()
            }

            content()
        }


    }
}




