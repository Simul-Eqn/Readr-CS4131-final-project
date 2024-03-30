// adapted from: https://github.com/YanneckReiss/JetpackComposeMLKitTutorial

package com.example.readr.camera

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.applyCanvas
import com.example.readr.MainActivity
import com.example.readr.Variables
import com.example.readr.data.ImageLoader
import com.example.readr.forceRecomposeWith
import com.example.readr.noRippleClickable
import com.example.readr.presentation.ChangeReplacedTextSizeSlider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt
import androidx.compose.material3.Text as ComposeText

// first 2 conversion functions taken from

private fun YUV_420_888toNV21(image: Image): ByteArray {
    val nv21: ByteArray
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    nv21 = ByteArray(ySize + uSize + vSize)

    //U and V are swapped
    yBuffer[nv21, 0, ySize]
    vBuffer[nv21, ySize, vSize]
    uBuffer[nv21, ySize + vSize, uSize]
    return nv21
}

private fun NV21toJPEG(nv21: ByteArray, width: Int, height: Int): ByteArray {
    val out = ByteArrayOutputStream()
    val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    yuv.compressToJpeg(Rect(0, 0, width, height), 100, out)
    return out.toByteArray()
}

class TextRecognitionAnalyzer(
    val view: @Composable()(PaddingValues)->Unit,
    private val setView: (@Composable()(PaddingValues)->Unit) -> Unit,
    val offsetY: Float,
    //var addOffsetX: Float,
    //val setAddOffsetX:(Float)->Unit,
    //var addOffsetY: Float,
    //val setAddOffsetY:(Float)->Unit,
    val frozen:Int,
    val setFrozen:(Int)->Unit,
    val sendNotification: (String, String) -> Unit,
    val setText: (String)->Unit,
) : ImageAnalysis.Analyzer {

    companion object {
        const val THROTTLE_TIMEOUT_MS = 1000L

        fun getScreenShot(view: View, withBitmap:(Bitmap)->Unit) {
            /*val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(returnedBitmap)
            val bgDrawable = view.background
            if (bgDrawable != null) bgDrawable.draw(canvas)
            else canvas.drawColor(android.graphics.Color.WHITE)
            view.draw(canvas)
            return returnedBitmap*/

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(Runnable {
                val bmp = Bitmap.createBitmap(view.width, view.height,
                    Bitmap.Config.ARGB_8888).applyCanvas {
                    view.draw(this)
                }
                withBitmap(bmp)
            }, 1000)
        }

        var addOffsetX = 0f
        var addOffsetY = 0f
    }

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    val imgl = ImageLoader()




    var notifAlready = false


    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        scope.launch {

            val mediaImage: Image = imageProxy.image ?: run { imageProxy.close(); return@launch }
            val inputImage: InputImage = InputImage.fromMediaImage(mediaImage, 90)//imageProxy.imageInfo.rotationDegrees)

            val imgWidth = mediaImage.width

            if (frozen == 1) {
                // save image to firebase as init image

                val data = NV21toJPEG(
                    YUV_420_888toNV21(mediaImage),
                    mediaImage.width, mediaImage.height,
                )

                // save to firebase storage
                imgl.withNextImgNum ({
                    imgl.saveImage(data, "image_${it}_init.png")
                })

            }

            suspendCoroutine { continuation ->

                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText: Text ->

                        System.out.println("DETECT SUCCESS YAY")


                        /*val detectedText: String = visionText.text
                        if (detectedText.isNotBlank()) {
                            onDetectedTextUpdated(detectedText)
                        }*/

                        if (!notifAlready) {

                            setView {

                                System.out.println("SUCCESSFULLY DETECED!! NUMBER OF TEXT BLOCKS: ${visionText.textBlocks.size}")

                                var tempText = ""

                                for (tbidx in visionText.textBlocks.indices) {

                                    System.out.println(
                                        "TEXTBLOCK TEXT: ${visionText.textBlocks[tbidx].text} " +
                                                "AT POSITIONS ${visionText.textBlocks[tbidx].boundingBox!!.left}, " +
                                                "${visionText.textBlocks[tbidx].boundingBox!!.top}, ${visionText.textBlocks[tbidx].boundingBox!!.right}" +
                                                " ${visionText.textBlocks[tbidx].boundingBox!!.bottom}"
                                    )

                                    tempText += visionText.textBlocks[tbidx].text + "\n\n"
                                }

                                setText(tempText)

                                //startForegroundService(intent)


                                // display them all
                                val dm: DisplayMetrics =
                                    MainActivity.context.resources.displayMetrics

                                fun pxToDP(px: Double): Int {
                                    return (px / (dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
                                }
                                fun pxToDP(px: Int): Int {
                                    return (px.toDouble() / (dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
                                }


                                var scale = dm.widthPixels.toDouble() / imgWidth.toDouble()
                                scale = scale * 1.35
                                System.out.println("SCALE: $scale")

                                //val offsetYpx = mediaImage.height - dm.heightPixels // this is in px
                                //val offsetY = pxToDP(offsetYpx) // MUST CONVERT TO DP

                                //System.out.println("OFFSETY: ${offsetY + addOffsetY}")


                                // show compose stuff

                                var fontSize by remember(Variables.overlayTextSize) {
                                    mutableFloatStateOf(
                                        Variables.overlayTextSize
                                    )
                                }

                                // get all the items and save
                                Box(modifier = Modifier.fillMaxSize().padding(it).noRippleClickable { MainActivity.window.decorView.apply { systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN } ; System.out.println("HIDING NAVBAR")}
                                ) {

                                    for (textBlock in visionText.textBlocks) {
                                        /*val height =
                                    pxToDP((textBlock.boundingBox!!.bottom - textBlock.boundingBox!!.top)).dp
                                val width =
                                    pxToDP((textBlock.boundingBox!!.right - textBlock.boundingBox!!.left)).dp*/

                                        if (pxToDP(textBlock.boundingBox!!.top*scale) > offsetY + addOffsetY) {

                                            Button(
                                                {
                                                    // SHOW POPUP OF TEXT
                                                },
                                                modifier = Modifier
                                                    .offset(
                                                        (pxToDP(textBlock.boundingBox!!.left*scale) + addOffsetX).dp,
                                                        (pxToDP(textBlock.boundingBox!!.top*scale) - (offsetY + addOffsetY)).dp
                                                    )

                                                    .padding(0.dp),
                                                shape = RectangleShape,
                                                contentPadding = PaddingValues(0.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.background
                                                ),
                                            ) {
                                                ComposeText(
                                                    textBlock.text,
                                                    modifier = Modifier.padding(0.dp).forceRecomposeWith(MaterialTheme.colorScheme.background),
                                                    style = TextStyle(
                                                        color = if (MainActivity.isDarkTheme) Color.White
                                                        else Color.Black,
                                                        fontSize = fontSize.sp,
                                                        fontFamily = Variables.overlayFontFamily
                                                    )
                                                )
                                            }

                                            //System.out.println("DISPLAYED TEXT: ${textBlock.text} AT POSITIONS ${textBlock.boundingBox!!.left}, ${textBlock.boundingBox!!.top}, ${textBlock.boundingBox!!.right} ${textBlock.boundingBox!!.bottom}")

                                        } else {
                                            //System.out.println("CHOSE NOT TO DISPLAY TEXT: ${textBlock.text}")
                                        }
                                    }

                                }

                                Box(
                                    modifier = Modifier.fillMaxSize().padding(it),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    ChangeReplacedTextSizeSlider(fontSize) {
                                        fontSize = it
                                        Variables.overlayTextSize = it
                                    }
                                }


                                if (frozen == 1) {
                                    // vertical adjustment slider - addOffsetY
                                    Box(
                                        contentAlignment = Alignment.CenterEnd,
                                        modifier = Modifier.fillMaxSize().padding(it),
                                    ) {
                                        Box(
                                            modifier = Modifier.wrapContentSize(),
                                            //.background(Color.Green)
                                        ) {
                                            val h = pxToDP(dm.heightPixels) - offsetY - 32
                                            Slider(
                                                modifier = Modifier
                                                    .graphicsLayer {
                                                        rotationZ = 270f
                                                        transformOrigin = TransformOrigin(0f, 0f)
                                                    }
                                                    .layout { measurable, constraints ->
                                                        val placeable = measurable.measure(
                                                            Constraints(
                                                                minWidth = constraints.minHeight,
                                                                maxWidth = constraints.maxHeight,
                                                                minHeight = constraints.minWidth,
                                                                maxHeight = constraints.maxHeight,
                                                            )
                                                        )
                                                        layout(placeable.height, placeable.width) {
                                                            placeable.place(-placeable.width, 0)
                                                        }
                                                    },
                                                value = addOffsetY,
                                                onValueChange = {
                                                    addOffsetY = it
                                                },
                                                //valueRange = -600f..600f,
                                                //steps = 199,
                                                valueRange = -(h / 2)..(h / 2),
                                                steps = 249,
                                            )
                                        }
                                    }


                                    // horizontal adjustment slider - addOffsetX
                                    Box(
                                        contentAlignment = Alignment.BottomCenter,
                                        modifier = Modifier.fillMaxSize().padding(it),
                                    ) {
                                        val w = (pxToDP(dm.widthPixels) - 32).toFloat()
                                        Box(
                                            modifier = Modifier.wrapContentSize(),
                                            //.background(Color.Green)
                                        ) {
                                            Slider(
                                                value = addOffsetX,
                                                onValueChange = {
                                                    addOffsetX = it
                                                },
                                                //valueRange = -400f..400f,
                                                //steps = 199,
                                                valueRange = -(w / 2)..(w / 2),
                                                steps = 249
                                            )
                                        }
                                    }

                                }


                            }

                        }

                        if (frozen == 1) {
                            setFrozen(2) // go to next stage: capturing final
                        }

                        System.out.println("RECOGNITION ANALYZER FROZEN: $frozen")


                        if ((frozen == 2) and (!notifAlready)) {


                            // SCREENSHOT AGAIN TO TRY TO SAVE
                            try {

                                // OLD: get bitmap of screen
                                //val finalBitmap = TextRecognitionAnalyzer.getScreenShot(MainActivity.window.decorView.rootView)

                                getScreenShot(MainActivity.window.decorView.rootView) { finalBitmap:Bitmap ->
                                    // SAVE AS FINAL
                                    imgl.withNextImgNum ({
                                        imgl.saveImage(finalBitmap, "image_${it}_final.png")
                                    })


                                    if (!notifAlready) { // check again in case of lag

                                        // show notification
                                        sendNotification(
                                            "Camera Usage Saved!",
                                            "Used Readr Camera Service. Return to app history page to view or clear usage history. "
                                        )

                                        notifAlready = true

                                    }

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

                            }

                            setFrozen(3) // fully freeze



                        }




                    }
                    .addOnCompleteListener {
                        continuation.resume(Unit)
                    }


            }


            delay(THROTTLE_TIMEOUT_MS)


        }.invokeOnCompletion { exception ->
            exception?.printStackTrace()
            imageProxy.close()
        }
    }
}

