// adapted from: https://github.com/YanneckReiss/JetpackComposeMLKitTutorial

package com.example.readr.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.graphics.Color as androidColor
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.readr.MainActivity
import com.example.readr.camera.TextRecognitionAnalyzer.Companion.getScreenShot
import com.example.readr.data.ImageLoader
import com.example.readr.noRippleClickable
import com.example.readr.presentation.onscaffold.DDItem
import com.example.readr.presentation.onscaffold.DisplayTopBar
import com.example.readr.ui.theme.LocalTextStyles
import kotlin.random.Random



@Composable
fun CameraScreen(backButtonFunc: () -> Unit, sendNotification: (String, String) -> Unit,
                 setReadText:(String)->Unit, goToReadTextScreen:()->Unit,
                 setOnback:((()->Unit)?)->Unit, initOnback:()->Unit, dropdownItems: MutableList<DDItem>,
                 camera:Boolean, setCamera:(Boolean)->Unit, ) {

    var overlayContent : @Composable()(PaddingValues)->Unit by remember { mutableStateOf({}) }

    //var addOffsetY:Float by remember { mutableFloatStateOf(0f) }
    //var addOffsetX:Float by remember { mutableFloatStateOf(0f) }

    setOnback({ MainActivity.textToSpeech.stop() ; initOnback()})


    ProvideTextStyle(TextStyle(color= MaterialTheme.colorScheme.onBackground)) {

        if (camera) { // camera

            var frozen: Int by remember { mutableIntStateOf(0) } // 0 means not frozen, 1 means capturing init, 2 means capturing final, 3 means captured and frozen.

            CameraContent(
                overlayContent,
                { overlayContent = it },
                // addOffsetX , { addOffsetX = it }, addOffsetY , { addOffsetY = it },
                backButtonFunc,
                frozen,
                { frozen = it },
                sendNotification,
                setReadText,
                goToReadTextScreen,
                dropdownItems,
                { setCamera(false) ; MainActivity.textToSpeech.stop() },
                setOnback,
                initOnback,
            )

        } else { // gallery
            setOnback({ setCamera(true) ; MainActivity.textToSpeech.stop() ; initOnback() })
            GalleryScreen({ setCamera(true) }, dropdownItems, sendNotification, setReadText,
                goToReadTextScreen, { setOnback { if (it != null) it{ setCamera(true) ; MainActivity.textToSpeech.stop(); initOnback() } } },
                { setCamera(true) ; MainActivity.textToSpeech.stop() ; initOnback() } )
        }

    }
}

@Composable
private fun CameraContent(overlayContent:@Composable()(PaddingValues)->Unit, setOverlayContent:(@Composable()(PaddingValues)->Unit)->Unit,
                          //addOffsetX: Float, setAddOffsetX:(Float)->Unit , addOffsetY: Float, setAddOffsetY:(Float)->Unit ,
                          backButtonFunc: () -> Unit,
                          frozen: Int, setFrozen: (Int)->Unit, sendNotification: (String, String) -> Unit,
                          setReadText:(String)->Unit, goToReadTextScreen:()->Unit,
                          dropdownItems: MutableList<DDItem>, switchToGallery:()->Unit,
                          setOnback:((()->Unit)?)->Unit, initOnback: () -> Unit) {

    when (frozen) {
        0 -> setOnback(initOnback)
        else -> setOnback({ saveToHistory(sendNotification, initOnback) }) // reset to default onback
    }
    //setOnback({ saveToHistory(sendNotification, initOnback) })

    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }


    var text by remember { mutableStateOf("") }

    var showTextDialog by remember { mutableStateOf(false) }
    ShowTextDialog(showTextDialog, text, {
        showTextDialog = false
    })


    var rotation by remember { mutableStateOf(1) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    DisplayTopBar("Camera Text Scanner", {
                        when (frozen) {
                            0 -> initOnback()
                            else -> { saveToHistory(sendNotification, initOnback) } // reset to default onback
                        }
                    }, dropdownItems)
                }
                     },
            floatingActionButton = {
                if (frozen == 0) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        // gallery button
                        Button(
                            onClick = { switchToGallery() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.LightGray),
                            modifier = Modifier
                                .offset(30.dp, (-30).dp)
                                .border(
                                    5.dp,
                                    color = androidx.compose.ui.graphics.Color.Gray,
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .size(80.dp)
                            ,
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Icon(
                                Icons.Filled.Photo,
                                "Gallery upload button",
                                modifier = Modifier.size(60.dp)
                            )
                        }

                    }

                }

            },
            floatingActionButtonPosition = FabPosition.End,
        ) { paddingValues: PaddingValues ->
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .noRippleClickable {
                            MainActivity.window.decorView.apply {
                                systemUiVisibility =
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
                            }; System.out.println("HIDING NAVBAR")
                        },
                    contentAlignment = androidx.compose.ui.Alignment.BottomCenter
                ) {

                    TextRecognitionAnalyzer.rotation = rotation*90

                    Box(
                        modifier = Modifier.wrapContentSize().rotate(if (frozen==0) (rotation-1)*90f else 0f),
                    ) {
                        key(frozen) {

                            if (frozen == 0) {

                                AndroidView(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                        /*.noRippleClickable {
                            setFrozen(0)
                        }*/
                                        .noRippleClickable {
                                            MainActivity.window.decorView.apply {
                                                systemUiVisibility =
                                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
                                            }; System.out.println("HIDING NAVBAR")
                                        },
                                    factory = { context ->
                                        PreviewView(context).apply {
                                            layoutParams = LinearLayout.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                            setBackgroundColor(androidColor.BLACK)
                                            implementationMode =
                                                PreviewView.ImplementationMode.COMPATIBLE
                                            scaleType = PreviewView.ScaleType.FILL_START
                                        }.also { previewView ->
                                            startTextRecognition(
                                                context = context,
                                                cameraController = cameraController,
                                                lifecycleOwner = lifecycleOwner,
                                                previewView = previewView,
                                                view = overlayContent,
                                                setView = setOverlayContent,
                                                topPadding = -paddingValues.calculateTopPadding().value,
                                                //addOffsetX = addOffsetX,
                                                //setAddOffsetX = setAddOffsetX,
                                                //addOffsetY = addOffsetY,
                                                //setAddOffsetY = setAddOffsetY,
                                                frozen = frozen,
                                                setFrozen = setFrozen,
                                                sendNotification = sendNotification,
                                                setText = { text = it },
                                            )
                                        }
                                    },
                                )

                            } else {
                                TextRecognitionAnalyzer.saveInitImage()

                                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                                    var recomposeBool by remember { mutableStateOf(false) }
                                    TextRecognitionAnalyzer.ShowFinalView(
                                        recomposeBool,
                                        { recomposeBool = !recomposeBool },
                                        { setFrozen(0) }
                                    )
                                }
                            }

                        }
                    }

                    /*Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(16.dp),
                text = detectedText,
            )*/

                    //System.out.println("RECOMPOSED OVERLAY CONTENT: ${overlayContent}")



                    //System.out.println("FROZEN: $frozen")

                    Box(modifier = Modifier
                        .padding(paddingValues)
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .noRippleClickable {
                            MainActivity.window.decorView.apply {
                                systemUiVisibility =
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
                            }; System.out.println("HIDING NAVBAR")
                        }.align(Alignment.BottomCenter)
                    ) {

                        if (frozen == 0) {

                            overlayContent(paddingValues)

                            Box(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .padding(bottom = 48.dp, start=50.dp, end=50.dp)
                                    .align(Alignment.BottomCenter)
                            ) {

                                // freeze button
                                Button(
                                    onClick = { setFrozen(1); System.out.println("IN BUTTON FROZEN: $frozen") },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.LightGray),
                                    modifier = Modifier
                                        .border(
                                            5.dp,
                                            color = androidx.compose.ui.graphics.Color.Gray,
                                            shape = CircleShape
                                        )
                                        .size(80.dp)
                                        .align(Alignment.BottomCenter),
                                    contentPadding = PaddingValues(0.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.Camera,
                                        "Camera capture button",
                                        modifier = Modifier.size(60.dp)
                                    )
                                }



                                // rotate
                                Button(
                                    onClick = { rotation=(rotation+1)%4 ; TextRecognitionAnalyzer.rotation = rotation*90 },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.LightGray),
                                    modifier = Modifier
                                        .border(
                                            5.dp,
                                            color = androidx.compose.ui.graphics.Color.Gray,
                                            shape = CircleShape
                                        )
                                        .size(80.dp)
                                        .align(Alignment.BottomEnd),
                                    contentPadding = PaddingValues(0.dp),
                                ) {
                                    Icon(
                                        Icons.Filled.Rotate90DegreesCw,
                                        "Rotate 90 degrees clockwise",
                                        modifier = Modifier.size(60.dp)
                                    )
                                }


                            }




                        } else {
                            // buttons
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .background(androidx.compose.ui.graphics.Color.White)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {

                                item { // retake button
                                    Button({ setFrozen(0) }) {
                                        Row(
                                            modifier = Modifier.wrapContentSize(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Camera, "Retake picture button")
                                            Text(
                                                "Retake picture",
                                                style = LocalTextStyles.current.m
                                            )
                                        }

                                    }
                                }

                                item { // send to focused reading
                                    Button({
                                        saveToHistory(sendNotification, {
                                            setReadText(text)
                                            goToReadTextScreen()
                                        })
                                    }) {
                                        Row(
                                            modifier = Modifier.wrapContentSize(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.ArrowRightAlt,
                                                "Send to focused reading button"
                                            )
                                            Text(
                                                "Send to focused reading",
                                                style = LocalTextStyles.current.m
                                            )
                                        }

                                    }
                                }

                                item { // select all button
                                    Button({
                                        showTextDialog = true
                                    }) {
                                        Row(
                                            modifier = Modifier.wrapContentSize(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Filled.SelectAll, "Select all button")
                                            Text("Select All", style = LocalTextStyles.current.m)
                                        }

                                    }
                                }
                            }
                        }

                    }



                }

            }
        }

    }
}

@Composable
fun ShowTextDialog(visible:Boolean, text:String, onDismiss:()->Unit) {
    if (visible) {
        Dialog(onDismissRequest = {
            onDismiss()
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .border(
                        width = 6.dp,
                        color = Color(40, 40, 40),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
            ) {

                val clipboardManager = LocalClipboardManager.current

                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LazyColumn(modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 30.dp, max = 200.dp)
                            .padding(16.dp)) {
                            item {Text(text, style = LocalTextStyles.current.l)}
                        }

                        Button({
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(MainActivity.context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.CopyAll, "Copy text button")
                        }
                    }

                }


            }

        }
    }
}


private fun startTextRecognition(
    context: Context,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    view: @Composable()(PaddingValues)->Unit,
    setView: (@Composable()(PaddingValues)->Unit) -> Unit,
    topPadding: Float,
    //addOffsetX: Float,
    //setAddOffsetX:(Float)->Unit,
    //addOffsetY: Float,
    //setAddOffsetY:(Float)->Unit,
    frozen: Int,
    setFrozen: (Int)->Unit,
    sendNotification: (String, String) -> Unit,
    setText: (String)->Unit,
) {

    //if (frozen==3) return

    cameraController.imageAnalysisTargetSize =
        CameraController.OutputSize(AspectRatio.RATIO_16_9)
    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        TextRecognitionAnalyzer(
            view,
            setView,
            topPadding,
            frozen,
            setFrozen,
            sendNotification,
            setText,
        )
    )

    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController

}

fun saveToHistory(sendNotification: (String, String) -> Unit, onDone:()->Unit) {
    // SCREENSHOT AGAIN TO TRY TO SAVE
    try {

        // OLD: get bitmap of screen
        //val finalBitmap = TextRecognitionAnalyzer.getScreenShot(MainActivity.window.decorView.rootView)

        val imgl = ImageLoader()

        getScreenShot(MainActivity.window.decorView.rootView) { finalBitmap: Bitmap ->
            // SAVE AS FINAL
            imgl.withNextImgNum ({
                imgl.saveImage(finalBitmap, "image_${it}_final.png")
            })


            // show notification
            sendNotification(
                "Camera Usage Saved!",
                "Used Readr Camera Service. Return to app history page to view or clear usage history. "
            )
            onDone()

        }


    } catch (e: Exception) {

        Log.println(
            Log.ASSERT,
            "FINAL SCREENSHOT ERROR",
            "CLDNT TAKE SCREENSHOT DURING CAMERA USAGE"
        )

        /*Toast.makeText(
            MainActivity.context,
            "FAILED TO SAVE CAMERA USAGE. ",
            Toast.LENGTH_SHORT
        ).show()*/

        // show notification
        sendNotification(
            "Camera Usage Not saved :(",
            "Failed to save usage of accessibility service. The app history page will not show this usage. "
        )

        onDone()

    }
}
