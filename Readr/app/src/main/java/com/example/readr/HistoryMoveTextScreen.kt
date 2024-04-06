package com.example.readr

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.readr.MainActivity
import com.example.readr.Variables
import com.example.readr.camera.ShowTextDialog
import com.example.readr.camera.TextRecognitionAnalyzer
import com.example.readr.data.ImageLoader
import com.example.readr.forceRecomposeWith
import com.example.readr.noRippleClickable
import com.example.readr.customcomposables.ChangeReplacedTextSizeSlider
import com.example.readr.presentation.onscaffold.DDItem
import com.example.readr.presentation.onscaffold.DisplayTopBar
import com.example.readr.presentation.themeswitcher.ShowBgToggler
import com.example.readr.presentation.themeswitcher.TextColorSwitcher
import com.example.readr.ui.theme.LocalTextStyles
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.roundToInt


@Composable
fun HistoryMoveTextScreen(
    selectedImageBmp:ImageBitmap, histItemNum:Int, dropdownItems: List<DDItem>,
    sendNotification: (String, String) -> Unit,
    setReadText: (String) -> Unit, goToReadTextScreen: () -> Unit,
    setOnback: (((()->Unit)->Unit)?)->Unit,
    exit:()->Unit,
) {


    var leaveFunc: (()->Unit)->Unit = {it()}
    val setLeaveFunc:((()->Unit)->Unit)->Unit = {newLeaveFunc:(()->Unit)->Unit ->
        leaveFunc = newLeaveFunc
        setOnback({ MainActivity.textToSpeech.stop() ; newLeaveFunc(it) })
    }



    var imgView by remember { mutableStateOf<@Composable()()->Unit>({}) }
    val setImgView: (@Composable()()->Unit)->Unit = { imgView = it }

    var view by remember { mutableStateOf<@Composable()()->Unit>({}) }
    val setView: (@Composable()()->Unit)->Unit = { view = it }

    var view2 by remember { mutableStateOf<@Composable()()->Unit>({}) }
    val setView2: (@Composable()()->Unit)->Unit = { view2 = it }


    setImgView {
        var imgCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }


        Image(
            selectedImageBmp,
            contentDescription = "",
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { imgCoords = it },
        )


        setView {

            var text by remember { mutableStateOf("") }


            var showTextDialog by remember { mutableStateOf(false) }
            ShowTextDialog(showTextDialog, text, {
                showTextDialog = false
            })

            val imgl = ImageLoader()

            val bitmap = selectedImageBmp.asAndroidBitmap().copy(Bitmap.Config.RGBA_F16, true)
            val textRecognizer: TextRecognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) // TextRecognizer.Builder(applicationContext).build()
            val img = InputImage.fromBitmap(bitmap, 0)

            // SAVE THIS IMAGE AS INITIAL IMAGE
            imgl.withNextImgNum({
                imgl.saveImage(bitmap, "image_${it}_init.png", MainActivity.context)
                System.out.println("TRYING TO SAVE INITIAL IMG YES")
            })

            System.out.println("TRYYY")


            textRecognizer.process(img)
                .addOnSuccessListener { visionText ->

                    // display them all
                    val dm: DisplayMetrics = MainActivity.context.resources.displayMetrics

                    fun pxToDP(px: Int): Int {
                        return Math.round(px / dm.density)
                    }

                    val offsetYpx = bitmap.height - dm.heightPixels // this is in px
                    val offsetY = pxToDP(offsetYpx) // MUST CONVERT TO DP

                    System.out.println("OFFSETY: $offsetY")

                    var tempText = ""
                    for (tbidx in visionText.textBlocks.indices) {
                        tempText += visionText.textBlocks[tbidx].text + "\n\n"
                    }
                    text = tempText

                    setView2 {

                        var addOffsetX by remember { mutableStateOf(0.0f) }
                        var addOffsetY by remember { mutableStateOf(0.0f) }
                        var bgColor by remember { mutableStateOf(0f) }
                        var displayBg by remember { mutableStateOf(false) }

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
                            System.out.println("SCALE: $scale")


                            // show compose stuff

                            var fontSize by remember(Variables.overlayTextSize) {
                                mutableFloatStateOf(
                                    Variables.overlayTextSize
                                )
                            }

                            // get all the items and show
                            Box(modifier = Modifier
                                .fillMaxSize()
                            ) {

                                for (textBlock in visionText.textBlocks) {
                                    if (true) {
                                        Button(
                                            {try {
                                                MainActivity.textToSpeech.speak(textBlock.text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
                                            } catch (e:Exception) {
                                                Toast.makeText(MainActivity.context, "Error reading text.", Toast.LENGTH_SHORT).show()
                                            }},
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
                                            Text(
                                                textBlock.text,
                                                modifier = Modifier.padding(0.dp).forceRecomposeWith(MaterialTheme.colorScheme.background),
                                                style = TextStyle(
                                                    color = Color(bgColor, bgColor, bgColor),
                                                    fontSize = fontSize.sp,
                                                    fontFamily = Variables.overlayFontFamily
                                                )
                                            )
                                        }
                                    }
                                }

                            }

                            Box( // change replaced text slider
                                modifier = Modifier.fillMaxSize(),
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
                                modifier = Modifier.fillMaxSize(),
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
                                modifier = Modifier.fillMaxSize().padding(bottom=50.dp),
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
                            { addOffsetX = it },
                            addOffsetY,
                            { addOffsetY = it },
                            bgColor,
                            { bgColor = it },
                            displayBg,
                            { TextRecognitionAnalyzer.displayBg = !displayBg ; displayBg = !displayBg })

                    }


                    Log.d("DISPLAY OVERLAY", "ADDED VIEW")


                }
                .addOnFailureListener {
                    System.out.println("UGHHHH DETECT ERRROR PEUOFHPWOUSJFPWEOFU:LD")
                    Log.e("DETECT ERROR", it.message, it)
                }


            // buttons
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {


                item { // send to focused reading
                    Button({
                        setReadText(text)
                        goToReadTextScreen()


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





    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { DisplayTopBar("History Item $histItemNum view", { leaveFunc(exit) }, dropdownItems) },
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .noRippleClickable {
                    MainActivity.window.decorView.apply {
                        systemUiVisibility =
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
                    }; System.out.println("HIDING NAVBAR")
                },
            contentAlignment = Alignment.BottomCenter
        ) {


            imgView()

            view()

            view2()


            /*ShowGalleryImage(selectedImageBmp,
                returnToCamera,
                sendNotification,
                { leaveFunc = it },
                setReadText,
                goToReadTextScreen,
                {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                setView,
            )*/

        }
    }



}


