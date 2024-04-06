// adapted from: https://github.com/YanneckReiss/JetpackComposeMLKitTutorial

package com.example.readr.camera

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.applyCanvas
import com.example.readr.MainActivity
import com.example.readr.Variables
import com.example.readr.data.ImageLoader
import com.example.readr.forceRecomposeWith
import com.example.readr.noRippleClickable
import com.example.readr.customcomposables.ChangeReplacedTextSizeSlider
import com.example.readr.presentation.themeswitcher.ShowBgToggler
import com.example.readr.presentation.themeswitcher.TextColorSwitcher
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
        var rotation = 90

        fun getScreenShot(view: View, withBitmap:(Bitmap)->Unit) {
            /*val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(returnedBitmap)
            val bgDrawable = view.background
            if (bgDrawable != null) bgDrawable.draw(canvas)
            else canvas.drawColor(android.graphics.Color.WHITE)
            view.draw(canvas)
            return returnedBitmap*/

            Toast.makeText(MainActivity.context, "Saving...", Toast.LENGTH_SHORT).show()
            System.out.println("GETTING SCREENSHOT")

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(Runnable {
                val bmp = Bitmap.createBitmap(view.width, view.height,
                    Bitmap.Config.ARGB_8888).applyCanvas {
                    view.draw(this)
                }
                System.out.println("CALLING WITH BITMAP")
                withBitmap(bmp)
            }, 1000)
        }

        var offsetY = 0f
        var addOffsetX = 0f
        var addOffsetY = 0f
        var bgColor = 0f
        var displayBg = false

        lateinit var prevBitmap: Bitmap

        fun saveInitImage() {
            val bitmap = prevBitmap.copy(Bitmap.Config.RGBA_F16, true)
            val imgl = ImageLoader()

            // SAVE THIS IMAGE AS INITIAL IMAGE
            imgl.withNextImgNum({
                imgl.saveImage(bitmap, "image_${it}_init.png", MainActivity.context)
                System.out.println("TRYING TO SAVE INITIAL IMG YES")
            })

            System.out.println("TRYYY")
        }

        @Composable
        fun ShowFinalView(recomposeBool:Boolean, recompose:()->Unit, goBack:()->Unit) {

            var imgView by remember { mutableStateOf<@Composable()()->Unit>({}) }
            val setImgView: (@Composable()()->Unit)->Unit = { imgView = it }

            var view by remember { mutableStateOf<@Composable()()->Unit>({}) }
            val setView: (@Composable()()->Unit)->Unit = { view = it }

            var view2 by remember { mutableStateOf<@Composable()()->Unit>({}) }
            val setView2: (@Composable()()->Unit)->Unit = { view2 = it }

            if (!(::prevBitmap.isInitialized)) {
                Toast.makeText(MainActivity.context, "Did not finish loading camera yet", Toast.LENGTH_SHORT).show()
                goBack()
                return
            }

            setImgView {
                var imgCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }


                Image(
                    prevBitmap.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { imgCoords = it }
                        .rotate(rotation - 90f)
                    ,
                )


                var text by remember { mutableStateOf("") }


                var showTextDialog by remember { mutableStateOf(false) }
                ShowTextDialog(showTextDialog, text, {
                    showTextDialog = false
                })

                val imgl = ImageLoader()

                val bitmap = prevBitmap.copy(Bitmap.Config.RGBA_F16, true)
                val textRecognizer: TextRecognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) // TextRecognizer.Builder(applicationContext).build()
                val img = InputImage.fromBitmap(bitmap, 0)




                setView {

                    textRecognizer.process(img)
                        .addOnSuccessListener { visionText ->

                            // display them all
                            val dm: DisplayMetrics = MainActivity.context.resources.displayMetrics

                            fun pxToDP(px: Int): Int {
                                return Math.round(px / dm.density)
                            }

                            //System.out.println("OFFSETY: $offsetY")

                            var tempText = ""
                            for (tbidx in visionText.textBlocks.indices) {
                                tempText += visionText.textBlocks[tbidx].text + "\n\n"
                            }
                            text = tempText

                            setView2 {

                                var addOffsetX by remember { mutableStateOf(TextRecognitionAnalyzer.addOffsetX) }
                                var addOffsetY by remember { mutableStateOf(TextRecognitionAnalyzer.addOffsetY) }
                                var bgColor by remember { mutableStateOf(Companion.bgColor) }
                                var displayBg by remember { mutableStateOf(Companion.displayBg) }

                                @Composable
                                fun DisplayText(
                                    addOffsetX: Float, setAddOffsetX: (Float) -> Unit,
                                    addOffsetY: Float, setAddOffsetY: (Float) -> Unit,
                                    bgColor: Float, setBgColor: (Float)->Unit,
                                    displayBg: Boolean, toggleDisplayBg: ()->Unit,
                                ) {

                                    fun pxToDP(px: Double): Int {
                                        return (px / (dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
                                    }

                                    fun pxToDP(px: Int): Int {
                                        return (px.toDouble() / (dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
                                    }


                                    val scale = (imgCoords!!.size.width).toDouble() / bitmap.width.toDouble()
                                    //System.out.println("SCALE: $scale")


                                    // show compose stuff

                                    var fontSize by remember(Variables.overlayTextSize) {
                                        mutableFloatStateOf(
                                            Variables.overlayTextSize
                                        )
                                    }

                                    // get all the items and show
                                    Box(modifier = Modifier
                                        .wrapContentSize()
                                        .offset(addOffsetX.dp, (-addOffsetY).dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                            //.forceRecomposeWith(recomposeBool)
                                        ) {
                                            for (textBlock in visionText.textBlocks) {
                                                System.out.println("REDRAWING TEXTBLOCKS")
                                                Button(
                                                    {
                                                        try {
                                                            MainActivity.textToSpeech.speak(textBlock.text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
                                                        } catch (e:Exception) {
                                                            Toast.makeText(MainActivity.context, "Error reading text.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .offset(
                                                            //(pxToDP(textBlock.boundingBox!!.left * scale) + addOffsetX).dp,
                                                            //(pxToDP(textBlock.boundingBox!!.top * scale) - (offsetY + addOffsetY)).dp
                                                            (pxToDP(textBlock.boundingBox!!.left * scale)).dp,
                                                            (pxToDP(textBlock.boundingBox!!.top * scale) - offsetY).dp
                                                        )
                                                        .padding(0.dp)
                                                    ,
                                                    shape = RectangleShape,
                                                    contentPadding = PaddingValues(0.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor =
                                                            if (displayBg) Color(1-bgColor, 1-bgColor, 1-bgColor)
                                                            else Color.Transparent

                                                    ),
                                                ) {
                                                    System.out.println("bgcolor: $bgColor AND COLOR: ${Color(bgColor, bgColor, bgColor)}")
                                                    Text(
                                                        buildAnnotatedString {
                                                            with (SpanStyle(color = Color(bgColor, bgColor, bgColor),)) {
                                                                append(textBlock.text)
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .padding(0.dp)
                                                            .forceRecomposeWith(MaterialTheme.colorScheme.background),
                                                        style = TextStyle(
                                                            fontSize = fontSize.sp,
                                                            fontFamily = Variables.overlayFontFamily
                                                        ),
                                                        color = Color(bgColor, bgColor, bgColor),
                                                    )
                                                }
                                            }

                                        }

                                    }

                                    Box(
                                        modifier = Modifier.fillMaxSize()//.padding(paddingValues)
                                        ,
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        Column(modifier = Modifier.wrapContentHeight(),) {
                                            ChangeReplacedTextSizeSlider(fontSize) {
                                                fontSize = it
                                                Variables.overlayTextSize = it
                                            }


                                            Row(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween) {

                                                Box(
                                                    modifier = Modifier.wrapContentSize()
                                                ) {
                                                    TextColorSwitcher(Math.round(bgColor) == 0) {
                                                        setBgColor(1 - bgColor)
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier.wrapContentSize()
                                                ) {
                                                    ShowBgToggler(displayBg, oppColor=bgColor) {
                                                        toggleDisplayBg()
                                                    }
                                                }

                                            }


                                        }

                                    }


                                    // vertical adjustment slider - addOffsetY
                                    Box(
                                        contentAlignment = Alignment.CenterEnd,
                                        modifier = Modifier.fillMaxSize()//.padding(paddingValues)
                                        ,
                                        //.padding(it)
                                    ) {
                                        Box(
                                            modifier = Modifier.wrapContentSize(),
                                            //.background(Color.Green)
                                        ) {
                                            val h =
                                                pxToDP(dm.heightPixels).toFloat() - offsetY - 32.0f
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
                                                    setAddOffsetY(it)
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
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(bottom = 80.dp)//.padding(paddingValues)
                                        ,
                                        //.padding(it)
                                    ) {
                                        val w = (pxToDP(dm.widthPixels) - 32).toFloat()
                                        Box(
                                            modifier = Modifier.wrapContentSize(),
                                            //.background(Color.Green)
                                        ) {
                                            Slider(
                                                value = addOffsetX,
                                                onValueChange = {
                                                    setAddOffsetX(it)
                                                },
                                                //valueRange = -400f..400f,
                                                //steps = 199,
                                                valueRange = -(w / 2)..(w / 2),
                                                steps = 249
                                            )
                                        }
                                    }

                                }


                                DisplayText(
                                    addOffsetX,
                                    { TextRecognitionAnalyzer.addOffsetX = it ; addOffsetX = it ; },
                                    addOffsetY,
                                    { TextRecognitionAnalyzer.addOffsetY = it ; addOffsetY = it ; },
                                    bgColor,
                                    { TextRecognitionAnalyzer.bgColor = it ; bgColor = it },
                                    displayBg,
                                    { TextRecognitionAnalyzer.displayBg = !displayBg ; displayBg = !displayBg })




                            }



                            Log.d("DISPLAY OVERLAY", "ADDED VIEW")



                        }
                        .addOnFailureListener {
                            System.out.println("UGHHHH DETECT ERRROR PEUOFHPWOUSJFPWEOFU:LD")
                            Log.e("DETECT ERROR", it.message, it)
                        }

                }

            }


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .forceRecomposeWith(recomposeBool)
                    ,
                contentAlignment = Alignment.BottomCenter
            ) {

                imgView()
                view()
                view2()

            }


        }

        fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(degrees)
            return Bitmap.createBitmap(
                source, 0, 0, source.width, source.height, matrix, true
            )
        }
    }

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    val imgl = ImageLoader()

    var notifAlready = false


    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        scope.launch {

            val mediaImage: Image = imageProxy.image ?: run { imageProxy.close(); return@launch }
            val inputImage: InputImage = InputImage.fromMediaImage(mediaImage, rotation)//imageProxy.imageInfo.rotationDegrees)

            val imgWidth = mediaImage.width

            val data = NV21toJPEG(
                YUV_420_888toNV21(mediaImage),
                mediaImage.width, mediaImage.height,
            )

            /*if (frozen == 1) {

                // save to firebase storage
                imgl.withNextImgNum ({
                    imgl.saveImage(data, "image_${it}_init.png")
                })

            }*/

            prevBitmap = rotateBitmap(imgl.ByteArrayToBitmap(data), 90f)

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
                                //scale = scale * 1.35
                                System.out.println("SCALE: $scale")

                                // show compose stuff

                                var fontSize by remember(Variables.overlayTextSize) {
                                    mutableFloatStateOf(
                                        Variables.overlayTextSize
                                    )
                                }

                                // get all the items and save
                                Box(modifier = Modifier
                                    .fillMaxSize()
                                    .padding(it)
                                    .noRippleClickable {
                                        MainActivity.window.decorView.apply {
                                            systemUiVisibility =
                                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
                                        }; System.out.println("HIDING NAVBAR")
                                    }
                                ) {

                                    for (textBlock in visionText.textBlocks) {
                                        /*val height =
                                    pxToDP((textBlock.boundingBox!!.bottom - textBlock.boundingBox!!.top)).dp
                                val width =
                                    pxToDP((textBlock.boundingBox!!.right - textBlock.boundingBox!!.left)).dp*/

                                        if (pxToDP(textBlock.boundingBox!!.top*scale) > offsetY + addOffsetY) {

                                            TextButton(
                                                {
                                                    try {
                                                        MainActivity.textToSpeech.speak(textBlock.text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
                                                    } catch (e:Exception) {
                                                        Toast.makeText(MainActivity.context, "Error reading text.", Toast.LENGTH_SHORT).show()
                                                    }
                                                // SHOW POPUP OF TEXT
                                                },
                                                modifier = Modifier
                                                    .offset(
                                                        (pxToDP(textBlock.boundingBox!!.left * scale) + addOffsetX).dp,
                                                        (pxToDP(textBlock.boundingBox!!.top * scale) - (offsetY + addOffsetY)).dp
                                                    )

                                                    .padding(0.dp),
                                                shape = RectangleShape,
                                                contentPadding = PaddingValues(0.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor =
                                                    if (displayBg) Color(1-bgColor, 1-bgColor, 1-bgColor)
                                                    else Color.Transparent

                                                ),
                                            ) {
                                                //System.out.println("bgcolor: $bgColor AND COLOR: ${Color(bgColor, bgColor, bgColor)}")

                                                Text(
                                                    textBlock.text,
                                                    modifier = Modifier
                                                        .padding(0.dp)
                                                        .forceRecomposeWith(MaterialTheme.colorScheme.background),
                                                    style = TextStyle(
                                                        fontSize = fontSize.sp,
                                                        fontFamily = Variables.overlayFontFamily
                                                    ),
                                                    color = Color(bgColor, bgColor, bgColor),
                                                )
                                            }

                                            //System.out.println("DISPLAYED TEXT: ${textBlock.text} AT POSITIONS ${textBlock.boundingBox!!.left}, ${textBlock.boundingBox!!.top}, ${textBlock.boundingBox!!.right} ${textBlock.boundingBox!!.bottom}")

                                        } else {
                                            //System.out.println("CHOSE NOT TO DISPLAY TEXT: ${textBlock.text}")
                                        }
                                    }

                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(it),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Column(modifier = Modifier.wrapContentHeight(),) {
                                        ChangeReplacedTextSizeSlider(fontSize) {
                                            fontSize = it
                                            Variables.overlayTextSize = it
                                        }


                                        /*Box(
                                            modifier = Modifier.wrapContentSize(),
                                            //.background(Color.Green)
                                        ) {
                                            Slider(
                                                value = bgColor,
                                                onValueChange = {
                                                    bgColor = it
                                                },
                                                //valueRange = -400f..400f,
                                                //steps = 199,
                                                valueRange = 0f..1f,
                                                steps = 99
                                            )
                                        }*/

                                        Row(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween) {

                                            Box(
                                                modifier = Modifier.wrapContentSize()
                                            ) {
                                                TextColorSwitcher(Math.round(bgColor) == 0) {
                                                    bgColor = (1 - bgColor)
                                                }
                                            }

                                            Box(
                                                modifier = Modifier.wrapContentSize()
                                            ) {
                                                ShowBgToggler(displayBg, oppColor=bgColor) {
                                                    displayBg = !displayBg
                                                }
                                            }

                                        }


                                    }
                                }


                                // vertical adjustment slider - addOffsetY
                                Box(
                                    contentAlignment = Alignment.CenterEnd,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(it),
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
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(it),
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

                        /*
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

                         */



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

