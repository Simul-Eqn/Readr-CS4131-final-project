package com.example.readr.accessibilitymenu

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.readr.R
import com.example.readr.Variables
import com.example.readr.data.ImageLoader
import com.example.readr.presentation.ChangeReplacedTextSizeSlider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.properties.Delegates


class AccessibilityMenu : AccessibilityService() {


    lateinit var notificationManager: NotificationManager
    val channelID = "com.example.readr.amenunotifications"
    var nextNotifId = 0

    fun createNotificationChannel(id:String, name:String, description:String) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(id, name, importance)
        channel.description = description
        channel.enableLights(true)
        channel.lightColor = android.graphics.Color.GREEN
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager.createNotificationChannel(channel)
    }

    fun sendNotification(title:String, text:String, ) {
        val notification = Notification.Builder(this, channelID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_background)
            //.setSmallIcon(R.drawable.appicon) TODO GET AN ICON
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please enable notifications.", Toast.LENGTH_SHORT).show()
        }

        notificationManager.notify(nextNotifId++, notification)
    }


    override fun onServiceConnected() {

        // prep notifications
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(channelID, "AMenuNotifs", "description")


        // check
        System.out.println("CAN DRAW OVERLAYS: ${Settings.canDrawOverlays(applicationContext)}")
        // Use this intent to enable permission
        if (!Settings.canDrawOverlays(applicationContext)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startForegroundService(intent) // (?)
            Log.d("PERMISSION", "CANNOT DRAW OVERLAY WOSDFHOWEND")
            // TODO CONT ASKING FOR IT
        }


        val imgl = ImageLoader()


        //System.out.println("ACCESSED YAY")

        //System.out.println(this.serviceInfo.capabilities)

        // get screenshot
        // create bitmap screen capture
        takeScreenshot(
            Display.DEFAULT_DISPLAY, mainExecutor,
            object : AccessibilityService.TakeScreenshotCallback {
                override fun onSuccess(screenshot: ScreenshotResult) {
                    //System.out.println("SUCCESSFULLY SCREENSHOTTED")
                    //Toast.makeText(applicationContext, "SUCCESSFULLY TOOK SCREENSHOT!", Toast.LENGTH_SHORT).show()

                    // get bitmap of screen
                    val bitmap = Bitmap.wrapHardwareBuffer(screenshot.hardwareBuffer, screenshot.colorSpace)!!.copy(Bitmap.Config.RGBA_F16, true)

                    val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) // TextRecognizer.Builder(applicationContext).build()

                    val img = InputImage.fromBitmap(bitmap, 0)

                    // SAVE THIS IMAGE AS INITIAL IMAGE
                    imgl.withNextImgNum {
                        imgl.saveImage(bitmap, "image_${it}_init.png", this@AccessibilityMenu)
                    }


                    textRecognizer.process(img)
                        .addOnSuccessListener {

                            // added intent to try to show w bubble service
                            //val intent = Intent(applicationContext, BubbleService::class.java)
                            //intent.putExtra("textblock_count", it.textBlocks.size)

                            System.out.println("SUCCESSFULLY DETECED!! NUMBER OF TEXT BLOCKS: ${it.textBlocks.size}")

                            for ( tbidx in it.textBlocks.indices ) {
                                //intent.putExtra("textblock_text_$tbidx", it.textBlocks[tbidx].text)
                                //intent.putExtra("textblock_bbox_$tbidx", it.textBlocks[tbidx].boundingBox)

                                System.out.println("TEXTBLOCK TEXT: ${it.textBlocks[tbidx].text} "+
                                        "AT POSITIONS ${it.textBlocks[tbidx].boundingBox!!.left}, "+
                                        "${it.textBlocks[tbidx].boundingBox!!.top}, ${it.textBlocks[tbidx].boundingBox!!.right}"+
                                        " ${it.textBlocks[tbidx].boundingBox!!.bottom}")
                            }

                            //startForegroundService(intent)


                            // display them all
                            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
                            val dm: DisplayMetrics = resources.displayMetrics

                            fun pxToDP(px:Int) : Int {
                                return Math.round(px/dm.density)
                            }

                            val offsetYpx = bitmap.height - dm.heightPixels // this is in px
                            val offsetY = pxToDP(offsetYpx) // MUST CONVERT TO DP

                            System.out.println("OFFSETY: $offsetY")

                            val owner = ComposeLifecycleOwner()

                            val newView = (ComposeView(this@AccessibilityMenu).apply {
                                setViewTreeLifecycleOwner(owner)
                                setViewTreeSavedStateRegistryOwner(owner)
                                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                                setContent {

                                    var fontSize by remember(Variables.overlayTextSize) { mutableFloatStateOf(Variables.overlayTextSize) }

                                    // get all the items and save
                                    Box(modifier = Modifier.fillMaxSize()) {

                                        for (textBlock in it.textBlocks) {
                                            /*val height =
                                                pxToDP((textBlock.boundingBox!!.bottom - textBlock.boundingBox!!.top)).dp
                                            val width =
                                                pxToDP((textBlock.boundingBox!!.right - textBlock.boundingBox!!.left)).dp*/

                                            if ( pxToDP(textBlock.boundingBox!!.top) > offsetY ) {

                                                Button(
                                                    {
                                                        // SHOW POPUP OF TEXT
                                                    },
                                                    modifier = Modifier
                                                        .offset(
                                                            pxToDP(textBlock.boundingBox!!.left).dp,
                                                            (pxToDP(textBlock.boundingBox!!.top) - offsetY).dp
                                                        )
                                                        /*.sizeIn(
                                                        minHeight = height,
                                                        maxHeight = height,
                                                        minWidth = width,
                                                        maxWidth = width
                                                    )*/
                                                        //.height(height)
                                                        //.width(width)
                                                        .padding(0.dp),
                                                    shape = RectangleShape,
                                                    contentPadding = PaddingValues(0.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color.White
                                                    ),
                                                ) {
                                                    Text(
                                                        textBlock.text,
                                                        modifier = Modifier.padding(0.dp),
                                                        style = TextStyle(
                                                            color = Color.Black,
                                                            fontSize = fontSize.sp,
                                                            fontFamily = Variables.overlayFontFamily
                                                        )
                                                    )
                                                }

                                                System.out.println("DISPLAYED TEXT: ${textBlock.text} AT POSITIONS ${textBlock.boundingBox!!.left}, ${textBlock.boundingBox!!.top}, ${textBlock.boundingBox!!.right} ${textBlock.boundingBox!!.bottom}")

                                            } else {
                                                System.out.println("CHOSE NOT TO DISPLAY TEXT: ${textBlock.text}")
                                            }
                                        }


                                        ChangeReplacedTextSizeSlider(fontSize) {
                                            fontSize = it
                                            Variables.overlayTextSize = it
                                        }

                                    }


                                    TextButton({
                                        disableSelf()
                                    },
                                        modifier = Modifier.wrapContentSize(),
                                    ) {
                                        //Text("X", fontSize=100.sp, color= Color.Red)
                                        Icon(Icons.Filled.Close, "Close button", tint=Color.Red,
                                            modifier = Modifier.size(100.dp))
                                    }


                                }
                            })

                            owner.attachToDecorView(newView)

                            owner.onCreate()
                            owner.onStart()
                            owner.onResume()

                            wm.addView(newView, WindowManager.LayoutParams(
                                //dm.widthPixels,
                                //ViewGroup.LayoutParams.WRAP_CONTENT,
                                WindowManager.LayoutParams.MATCH_PARENT,
                                WindowManager.LayoutParams.MATCH_PARENT,
                                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                PixelFormat.TRANSLUCENT,
                            ))


                            Log.d("DISPLAY OVERLAY", "ADDED VIEW")


                            Thread {

                                Thread.sleep(3000) // wait for a while to load yes

                                // SCREENSHOT AGAIN TO TRY TO SAVE
                                takeScreenshot(
                                    Display.DEFAULT_DISPLAY, mainExecutor,
                                    object : AccessibilityService.TakeScreenshotCallback {
                                        override fun onSuccess(screenshot: ScreenshotResult) {
                                            //System.out.println("SUCCESSFULLY SCREENSHOTTED")
                                            //Toast.makeText(applicationContext, "SUCCESSFULLY TOOK SCREENSHOT!", Toast.LENGTH_SHORT).show()

                                            // get bitmap of screen
                                            val finalBitmap = Bitmap.wrapHardwareBuffer(
                                                screenshot.hardwareBuffer,
                                                screenshot.colorSpace
                                            )!!.copy(Bitmap.Config.RGBA_F16, true)

                                            // SAVE AS FINAL
                                            imgl.withNextImgNum {
                                                imgl.saveImage(
                                                    finalBitmap,
                                                    "image_${it}_final.png",
                                                    this@AccessibilityMenu
                                                )
                                            }

                                            // show notification
                                            sendNotification(
                                                "Accessibility Service Usage Saved!",
                                                "Used Readr Accessibility Service. Return to app history page to view or clear usage history. "
                                            )

                                        }

                                        override fun onFailure(errorCode: Int) {
                                            Log.println(
                                                Log.ASSERT,
                                                "FINAL SCREENSHOT ERROR",
                                                "ERROR CODE: $errorCode"
                                            )

                                            Toast.makeText(
                                                applicationContext,
                                                "FAILED TO SAVE ACCESSIBILITY SERVICE USAGE. ERROR CODE: $errorCode",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // show notification
                                            sendNotification(
                                                "Accessibility Service Usage Not saved :(",
                                                "Failed to save usage of accessibility service. The app history page will not show this usage. "
                                            )
                                        }
                                    }
                                )


                            }.start()



                            //disableSelf()

                            //Log.d("UGH", "HAVENT MANAGED TO DISABLE SELF")

                        }
                        .addOnFailureListener {
                            System.out.println("UGHHHH DETECT ERRROR PEUOFHPWOUSJFPWEOFU:LD")
                            Log.e("DETECT ERROR", it.message, it)
                        }



                    /*

                    xml attempt
                    // display them all - deal with non=compose stuff
                    val wm = getSystemService(WINDOW_SERVICE) as WindowManager
                    mLayout = FrameLayout(this@AccessibilityMenu)
                    val lp = WindowManager.LayoutParams()
                    lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                    lp.format = PixelFormat.TRANSLUCENT
                    lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    lp.width = WindowManager.LayoutParams.WRAP_CONTENT
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                    lp.gravity = Gravity.TOP
                    val inflater = LayoutInflater.from(this@AccessibilityMenu)
                    val view = inflater.inflate(R.layout.compose_layout, mLayout)

                    // add compose view
                    val composeView = view.findViewById<ComposeView>(R.id.compose_view)
                    composeView.apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            val dm: DisplayMetrics = resources.displayMetrics

                            // get all the items and save
                            Box( modifier = Modifier.fillMaxSize()  ) {
                                for (idx in 0..<items.size()) {
                                    val item = items.valueAt(idx)
                                    val height = Math.round((item.boundingBox.bottom - item.boundingBox.top)/dm.density).dp
                                    val width = Math.round((item.boundingBox.right - item.boundingBox.left)/dm.density).dp
                                    Button({
                                        // SHOW POPUP OF TEXT
                                    }, modifier = Modifier
                                        .offset(
                                            Math.round(item.boundingBox.left / dm.density).dp,
                                            Math.round(item.boundingBox.top / dm.density).dp
                                        )
                                        .sizeIn(
                                            minHeight = height,
                                            maxHeight = height,
                                            minWidth = width,
                                            maxWidth = width
                                        )
                                    ) {
                                        Text(item.value)
                                    }

                                    System.out.println("DISPLAYED TEXT: ${item.value} AT POSITIONS ${item.boundingBox.left}, ${item.boundingBox.top}, ${item.boundingBox.right} ${item.boundingBox.bottom}")
                                }
                            }

f
                            Text("WINDOW")
                        }
                    }

                    wm.addView(mLayout, lp)

                    */





                    /* this was an attempt to use google vision api, but it seems not needed

                    // convert to bytestring

                    var byteBuffer = ByteBuffer.allocate(1)

                    try {
                        byteBuffer = ByteBuffer.allocate(bitmap.byteCount+5)
                    } catch (e:Exception) {
                        Toast.makeText(applicationContext, "NOT ENOUGH RAM TO TAKE SCREENSHOT", Toast.LENGTH_SHORT).show()
                        return
                    }

                    bitmap.copyPixelsToBuffer(byteBuffer)
                    val byteArray = byteBuffer.array()
                    val byteString = ByteString.copyFrom(byteArray)

                    // make google vision api request
                    val img = Image.newBuilder().setContent(byteString).build() // set image
                    val feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build() // detect text
                    val request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build()
                    val requests = listOf(request)

                    // send request with client
                    try {
                        // initialize client
                        val client = ImageAnnotatorClient.create()

                        // get responses
                        val responses = client.batchAnnotateImages(requests).responsesList

                        // draw stuff if responses
                        for (res: AnnotateImageResponse in responses) {
                            if (res.hasError()) {
                                Log.e("ANNOTATE IMAGE ERROR", res.error.message)
                                Log.d("ANNOTATE IMAGE ERROR", "ERROR: ${res.error.message}")
                            } else {
                                for (annotation: EntityAnnotation in res.textAnnotationsList) {
                                    val offset = annotation.boundingPoly.getVertices(0)
                                    System.out.println("AT OFFSET (${offset.x}, ${offset.y}), THERE IS TEXT: ${annotation.description}")
                                    // TODO: DISPLAY TEXT ON SCREEN
                                }
                            }
                        }

                        client.close()
                    } catch (e:Exception) {
                        e.message?.let { Log.e("ANNOTATE IMAGE ERROR", it) }
                        Log.d("ANNOTATE IMAGE ERROR", "ERROR: ${e.message}")
                    }


                     */

                }

                override fun onFailure(errorCode: Int) {
                    Log.println(Log.ASSERT, "TAKE SCREENSHOT ERROR", "ERROR CODE: $errorCode")
                    Toast.makeText(applicationContext, "FAILED TO TAKE SCREENSHOT. ERROR CODE: $errorCode", Toast.LENGTH_SHORT).show()
                }

            }
        )

        //System.out.println("AFT TRYIGN TO TAKE SCREENSHOT")

    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }



    override fun onInterrupt() {

    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

}