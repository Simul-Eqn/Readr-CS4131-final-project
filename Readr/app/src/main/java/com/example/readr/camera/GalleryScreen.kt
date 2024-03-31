package com.example.readr.camera

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.example.readr.data.ImageLoader
import com.example.readr.forceRecomposeWith
import com.example.readr.noRippleClickable
import com.example.readr.customcomposables.ChangeReplacedTextSizeSlider
import com.example.readr.presentation.onscaffold.DDItem
import com.example.readr.presentation.onscaffold.DisplayTopBar
import com.example.readr.ui.theme.LocalTextStyles
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.roundToInt


@Composable
fun GalleryScreen(
    returnToCamera: () -> Unit, dropdownItems: List<DDItem>,
    sendNotification: (String, String) -> Unit,
    setReadText: (String) -> Unit, goToReadTextScreen: () -> Unit,
    setOnback: (((()->Unit)->Unit)?)->Unit,
    exit:()->Unit,
) {
    /*
    // Registers a photo picker activity launcher in single-select mode.
    val pickMedia = MainActivity.registerForActivityResult { uri: Uri? ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }*/

    var selectedImageBmp by remember { mutableStateOf(ImageBitmap(100, 100))}


    var leaveFunc: (()->Unit)->Unit = {}
    val setLeaveFunc:((()->Unit)->Unit)->Unit = {newLeaveFunc:(()->Unit)->Unit ->
        leaveFunc = newLeaveFunc
        setOnback({ newLeaveFunc(it) })
    }

    var imgView by remember { mutableStateOf<@Composable()()->Unit>({}) }
    val setImgView: (@Composable()()->Unit)->Unit = { imgView = it }

    var view by remember { mutableStateOf<@Composable()()->Unit>({}) }
    val setView: (@Composable()()->Unit)->Unit = { view = it }

    var view2 by remember { mutableStateOf<@Composable()()->Unit>({}) }
    val setView2: (@Composable()()->Unit)->Unit = { view2 = it }

    lateinit var photoPickerLauncher : ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>

    val retake = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    photoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(), onResult = {

        if (it == null) {
            returnToCamera()
            return@rememberLauncherForActivityResult
        }

        selectedImageBmp = MediaStore.Images.Media.getBitmap(MainActivity.context.contentResolver, it).copy(Bitmap.Config.RGBA_F16, true).asImageBitmap()

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

                            @Composable
                            fun DisplayText(
                                addOffsetX: Float, setAddOffsetX: (Float) -> Unit,
                                addOffsetY: Float, setAddOffsetY: (Float) -> Unit
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
                                                {},
                                                modifier = Modifier
                                                    .offset(
                                                        (pxToDP(textBlock.boundingBox!!.left * scale) + addOffsetX).dp,
                                                        (pxToDP(textBlock.boundingBox!!.top * scale) - (offsetY + addOffsetY)).dp
                                                    )
                                                    .padding(0.dp),
                                                shape = RectangleShape,
                                                contentPadding = PaddingValues(0.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.background
                                                ),
                                            ) {
                                                Text(
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
                                        }
                                    }

                                }

                                Box(
                                    modifier = Modifier.fillMaxSize()//.padding(paddingValues)
                                    ,
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    ChangeReplacedTextSizeSlider(fontSize) {
                                        fontSize = it
                                        Variables.overlayTextSize = it
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
                                    modifier = Modifier.fillMaxSize().padding(bottom=50.dp)//.padding(paddingValues)
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
                                { addOffsetX = it },
                                addOffsetY,
                                { addOffsetY = it })




                        }



                        Log.d("DISPLAY OVERLAY", "ADDED VIEW")

                        setLeaveFunc {
                            System.out.println("LEAVEFUNC CALLED")
                            try {
                                TextRecognitionAnalyzer.getScreenShot(MainActivity.window.decorView.rootView) { finalBitmap: Bitmap ->
                                    // SAVE AS FINAL
                                    imgl.withNextImgNum({
                                        imgl.saveImage(finalBitmap, "image_${it}_final.png")
                                    })

                                    // show notification
                                    sendNotification(
                                        "Camera Usage Saved!",
                                        "Used Readr Camera Service. Return to app history page to view or clear usage history. "
                                    )

                                    it()

                                }


                            } catch (e: Exception) {

                                Log.println(
                                    Log.ASSERT,
                                    "FINAL SCREENSHOT ERROR",
                                    "CLDNT TAKE SCREENSHOT DURING CAMERA USAGE"
                                )

                                // show notifications
                                sendNotification(
                                    "Camera Usage Not saved :(",
                                    "Failed to save usage of accessibility service. The app history page will not show this usage. "
                                )

                                it()

                            }
                        }


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

                    item { // retake button
                        Button({
                            retake()
                        }) {
                            Row(
                                modifier = Modifier.wrapContentSize(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Camera, "Retake picture button")
                                Text(
                                    "Choose different picture",
                                    style = LocalTextStyles.current.m
                                )
                            }

                        }
                    }

                    item { // send to focused reading
                        Button({
                            try {
                                TextRecognitionAnalyzer.getScreenShot(MainActivity.window.decorView.rootView) { finalBitmap: Bitmap ->
                                    // SAVE AS FINAL
                                    imgl.withNextImgNum({
                                        imgl.saveImage(finalBitmap, "image_${it}_final.png")
                                    })

                                    // show notification
                                    sendNotification(
                                        "Camera Usage Saved!",
                                        "Used Readr Camera Service. Return to app history page to view or clear usage history. "
                                    )

                                    setReadText(text)
                                    goToReadTextScreen()

                                }


                            } catch (e: Exception) {

                                Log.println(
                                    Log.ASSERT,
                                    "FINAL SCREENSHOT ERROR",
                                    "CLDNT TAKE SCREENSHOT DURING CAMERA USAGE"
                                )

                                // show notifications
                                sendNotification(
                                    "Camera Usage Not saved :(",
                                    "Failed to save usage of accessibility service. The app history page will not show this usage. "
                                )

                                setReadText(text)
                                goToReadTextScreen()

                            }

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


    })



    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { DisplayTopBar("Gallery Text Scanner", { leaveFunc(exit) }, dropdownItems) },
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




    LaunchedEffect(photoPickerLauncher) {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

}


