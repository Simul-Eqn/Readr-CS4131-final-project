// adapted from: https://github.com/YanneckReiss/JetpackComposeMLKitTutorial

package com.example.readr.camera

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.AspectRatio
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.readr.noRippleClickable
import com.example.readr.presentation.onscaffold.DisplayTopBar
import kotlin.random.Random



@Composable
fun CameraScreen(backButtonFunc: () -> Unit, sendNotification: (String, String) -> Unit) {

    var overlayContent : @Composable()(PaddingValues)->Unit by remember { mutableStateOf({}) }

    var addOffsetY:Float by remember { mutableFloatStateOf(0f) }

    var frozen:Int by remember { mutableIntStateOf(0) } // 0 means not frozen, 1 means capturing init, 2 means capturing final, 3 means captured and frozen.

    CameraContent( overlayContent , { overlayContent = it } , addOffsetY , { addOffsetY = it }, backButtonFunc,
        frozen, { frozen = it }, sendNotification )
}

@Composable
private fun CameraContent(overlayContent:@Composable()(PaddingValues)->Unit, setOverlayContent:(@Composable()(PaddingValues)->Unit)->Unit,
                          addOffsetY: Float, setAddOffsetY:(Float)->Unit , backButtonFunc: () -> Unit,
                          frozen: Int, setFrozen: (Int)->Unit, sendNotification: (String, String) -> Unit ) {

    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }



    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { DisplayTopBar("Text Scanner", backButtonFunc) },
    ) { paddingValues: PaddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.BottomCenter
        ) {

            key(addOffsetY, frozen) {

                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .noRippleClickable {
                            setFrozen(0)
                        },
                    factory = { context ->
                        PreviewView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(Color.BLACK)
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
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
                                addOffsetY = addOffsetY,
                                setAddOffsetY = setAddOffsetY,
                                frozen = frozen,
                                setFrozen = setFrozen,
                                sendNotification = sendNotification,
                            )
                        }
                    },
                )

            }

            /*Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(16.dp),
                text = detectedText,
            )*/

            System.out.println("RECOMPOSED OVERLAY CONTENT: ${overlayContent}")

            overlayContent(paddingValues)

            System.out.println("FROZEN: $frozen")

            Box(modifier = Modifier.padding(paddingValues).wrapContentSize()) {

                if (frozen == 0) {
                    // freeze button
                    Button(
                        onClick = { setFrozen(1); System.out.println("IN BUTTON FROZEN: $frozen") },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.LightGray),
                        modifier = Modifier.border(
                            5.dp,
                            color = androidx.compose.ui.graphics.Color.DarkGray,
                            shape = CircleShape
                        )
                            .size(50.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) { Icon(Icons.Filled.Camera, "Camera capture button") }

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

                        item { // send to focused reading
                            Button({
                                // TODO
                            }) {
                                Row(modifier = Modifier.wrapContentSize(), horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Filled.ArrowRightAlt, "Send to focused reading button")
                                    Text("Send to focused reading")
                                }

                            }
                        }

                        item { // select all button
                            Button({
                                // TODO - prob show a dialog yes
                            }) {
                                Row(modifier = Modifier.wrapContentSize(), horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Filled.SelectAll, "Select all button")
                                    Text("Select All")
                                }

                            }
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
    addOffsetY: Float,
    setAddOffsetY:(Float)->Unit,
    frozen: Int,
    setFrozen: (Int)->Unit,
    sendNotification: (String, String) -> Unit,
) {

    if (frozen==3) return

    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        TextRecognitionAnalyzer(view, setView, topPadding, addOffsetY, setAddOffsetY, frozen, setFrozen, sendNotification)
    )

    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController
}