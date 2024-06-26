package com.example.readr

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import com.example.readr.camera.CameraScreen
import com.example.readr.customcomposables.AdaptiveText
import com.example.readr.customcomposables.ChangeFontFamilyItem
import com.example.readr.customcomposables.ChangeReplacedTextSizeSlider
import com.example.readr.customcomposables.ChangeTextScaleSlider
import com.example.readr.customcomposables.ForceShowBottomColumn
import com.example.readr.customcomposables.ForceShowRightRow
import com.example.readr.data.FirebaseHandler
import com.example.readr.data.ImageLoader
//import com.example.readr.AccessibilityMenuService.LocalBinder
import com.example.readr.data.PersistentStorage
import com.example.readr.presentation.onboarding.OnBoardingScreen
import com.example.readr.presentation.onscaffold.BottomBar
import com.example.readr.presentation.onscaffold.COLLAPSED_TOP_BAR_HEIGHT
import com.example.readr.presentation.onscaffold.DDItem
import com.example.readr.presentation.onscaffold.DisplayTopBar
import com.example.readr.presentation.onscaffold.WrapInColllapsedTopBar
import com.example.readr.presentation.themeswitcher.ThemeSwitcher
import com.example.readr.ui.theme.LocalMoreColors
import com.example.readr.ui.theme.LocalReplacedTextStyles
import com.example.readr.ui.theme.LocalSpacings
import com.example.readr.ui.theme.LocalTextStyles
import com.example.readr.ui.theme.ReadrTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.vision.CameraSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.min
import kotlin.random.Random.Default.nextInt


class MainActivity : ComponentActivity() {

    companion object {
        lateinit var context: Context
        lateinit var window: Window
        lateinit var registerForActivityResult: ((Uri?)->Unit) -> ActivityResultLauncher<PickVisualMediaRequest>
        var isDarkTheme:Boolean = false

        val histItems = mutableListOf<HistoryItem>()
        var loadedHistItems = 0

        fun pxToDP(px: Int): Float {
            return (px / context.resources.displayMetrics.density)
        }


        // TTS
        lateinit var textToSpeech: TextToSpeech

    }

    // in main view, 0. other outernavigation things do not have tabs (?)
    val tab_titles:MutableList<String> = mutableListOf()
    val tab_images:MutableList<Any> = mutableListOf()
    var topBarImgs:MutableList<Any> = mutableListOf()

    var dropdownItems:MutableList<DDItem> = mutableListOf()

    var outerNavPageNo:Int = 0
    var innerNavTabNo:Int = 1


    // STT
    private lateinit var speechRecognizer: SpeechRecognizer
    var should_listen = false

    // IMGL
    private val imgl = ImageLoader()

    // media player
    var mediaPlayer:MediaPlayer? = null



    // CAMERA
    lateinit var cameraSource: CameraSource

    // onboarding page numbers, for help
    val onboardingInitPageNum = mapOf<Int, Int?>( // first digit (tens) is outernav, second (ones) is innernav
        0 to 13, // reading view
        1 to 5, // dashboard
        2 to 8, // settings
        10 to 9, // camera/gallery
        11 to 9, // camera/gallery
        12 to 9, // camera/gallery
        20 to 6, // history item
        21 to 6, // history item
        22 to 6, // history item
        30 to 6, // history item
        31 to 6, // history item
        32 to 6, // history item
        40 to 6, // history item
        41 to 6, // history item
        42 to 6, // history item
    )

    val onboardingEndPageNum = mapOf<Int, Int?>( // first digit (tens) is outernav, second (ones) is innernav
        0 to 14, // reading view
        1 to 5, // dashboard
        2 to 8, // settings
        10 to 12, // camera/gallery
        11 to 12, // camera/gallery
        12 to 12, // camera/gallery
        20 to 7, // history item
        21 to 7, // history item
        22 to 7, // history item
        30 to 7, // history item
        31 to 7, // history item
        32 to 7, // history item
        40 to 7, // history item
        41 to 7, // history item
        42 to 7, // history item
    )


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

    fun initTTS(): Boolean {
        return try {
            textToSpeech = TextToSpeech(this){ status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.language = Locale.UK
                }
            }
            val voiceObj = Voice("en-us-x-sfg#male_1-local", Locale.getDefault(),
                1, 1, false, null)
            textToSpeech.voice = voiceObj
            true
        } catch (e:Exception) {
            Log.e("INIT TTS", e.message!!)
            e.printStackTrace()
            false
        }
    }

    fun initSTT(onBeginSpeech:()->Unit, onResults: (ArrayList<String>?) -> Unit,
                onPartialResults: (ArrayList<String>?) -> Unit ,
                restart: ()->Unit ): Intent {
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        sttIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // yes

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) { }
            override fun onBeginningOfSpeech() { onBeginSpeech() }
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() { restart() }
            override fun onError(i: Int) { restart() }
            override fun onResults(bundle: Bundle) {
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                onResults(data)
            }
            override fun onPartialResults(bundle: Bundle) {
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                onPartialResults(data)
                //speechRecognizer.startListening(sttIntent)
            }
            override fun onEvent(i: Int, bundle: Bundle) {}
        })

        return sttIntent
    }

    var darkTheme:Boolean = false

    lateinit var fh:FirebaseHandler


    // notifications

    lateinit var notificationManager: NotificationManager
    val channelID = "com.example.readr.mainnotifications"
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
            .setSmallIcon(R.drawable.appicon)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please enable notifications.", Toast.LENGTH_SHORT).show()
        }

        notificationManager.notify(nextNotifId++, notification)
    }

    fun loadHistItems(onFinished:()->Unit) {
        histItems.clear()
        loadedHistItems = 0
        // history
        imgl.withNextImgNum({
            for (i in 0..<it) {
                histItems.add(HistoryItem(i))
            }
            Thread {
                while (loadedHistItems != it * 2) Thread.sleep(100)
                onFinished()
            }.start()
        })
    }

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MainActivity.context = applicationContext
        MainActivity.window = window
        MainActivity.registerForActivityResult = { registerForActivityResult(ActivityResultContracts.PickVisualMedia(), it) }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(channelID, "MainNotifs", "description")

        initOnScaffolds()

        // stt
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)


        val cursor = PersistentStorage.DB!!.rawQuery("SELECT ${PersistentStorage.rdm} FROM ${PersistentStorage.TABLE_NAME} ORDER BY ${PersistentStorage.id} DESC ;", null)
        //val cursor = contentResolver.query(Uri.parse(PersistentStorage.URL), null, null, null, null)
        var temp = false
        if (cursor!!.moveToFirst()) {
            temp = true
            darkTheme = (cursor!!.getInt(cursor!!.getColumnIndex(PersistentStorage.rdm)) == 1)
            MainActivity.isDarkTheme = darkTheme
        }
        cursor.close()


        setContent {

            var finishedOnboarding by remember { mutableStateOf(temp) }
            var onboardingInitPage:Int? by remember { mutableStateOf<Int?>(null) }
            var onboardingEndPage:Int? by remember { mutableStateOf<Int?>(null) }

            var localDarkTheme by remember { mutableStateOf(darkTheme) }
            var viewNo by remember(outerNavPageNo) { mutableIntStateOf(outerNavPageNo) }



            var recomposeBool by remember { mutableStateOf(true) }
            var firstOne by remember { mutableStateOf(true) }

            fun recomposeOuter() { recomposeBool = !recomposeBool ; firstOne = false }


            var loaded by remember { mutableStateOf(false) }

            // history
            LaunchedEffect(true) {
                loadHistItems({ loaded = true })
            }


            // dark mode transition prep
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val screenShotState = rememberScreenshotState()

            val offsetX = remember { mutableFloatStateOf(0f) }
            val offsetY = remember { mutableFloatStateOf(0f) }

            val scope = rememberCoroutineScope()
            val screenWidth = LocalConfiguration.current.screenWidthDp
            val screenWidthPx = with(LocalDensity.current) { screenWidth.dp.toPx() }

            val screenHeight = LocalConfiguration.current.screenHeightDp
            val screenHeightPx = with(LocalDensity.current) { screenHeight.dp.toPx() }

            val animationOffsetX =
                animateFloatAsState(
                    targetValue = offsetX.floatValue,
                    label = "animation offset",
                    finishedListener = {
                        offsetX.floatValue = 0f
                        screenShotState.setBitmap(null)
                    },
                    animationSpec = tween(1500)
                )

            val animationOffsetY =
                animateFloatAsState(
                    targetValue = offsetY.floatValue,
                    label = "animation offset",
                    finishedListener = {
                        offsetY.floatValue = 0f
                        screenShotState.setBitmap(null)
                    },
                    animationSpec = tween(1500)
                )



            if (dropdownItems.size < 2) {

                // set text size
                fh = FirebaseHandler()
                fh.loadTextSizes {
                    Variables.overlayTextSize = it
                    firstOne = false
                    recomposeOuter()
                }

                // add theme switcher
                dropdownItems.add(
                    DDItem(
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                horizontalArrangement = Arrangement.spacedBy(LocalSpacings.current.m),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                var textHeight by remember { mutableStateOf(0f) }
                                Text("Theme: ", style = LocalTextStyles.current.m,
                                    modifier = Modifier.wrapContentSize(),
                                    onTextLayout = { textHeight = pxToDP(it.size.height) ; System.out.println("THEME TEXT HEIGHT: $textHeight") })
                                ThemeSwitcher(darkTheme = localDarkTheme, size=Math.max(((30*Variables.textScale)*(if (Variables.textFontFamily == openDyslexic) 1f else 0.5f)), 30f).dp, onClick = {

                                    /*if ((outerNavPageNo == 1) and (innerNavTabNo == 1)) {
                                        Toast.makeText(this@MainActivity, "Cannot switch theme while using camera/gallery.", Toast.LENGTH_SHORT).show()
                                        return@ThemeSwitcher
                                    }*/

                                    scope.launch {
                                        screenShotState.capture()
                                        offsetX.floatValue = screenWidthPx
                                        offsetY.floatValue = screenHeightPx
                                        delay(400)

                                        darkTheme = !darkTheme
                                        localDarkTheme = darkTheme

                                        val values = ContentValues()
                                        values.put(PersistentStorage.rdm, if (darkTheme) 1 else 0)
                                        contentResolver.insert(PersistentStorage.CONTENT_URI, values)

                                        MainActivity.isDarkTheme = darkTheme
                                    }

                                })
                            }
                        },
                        { System.out.println("DARK THEME: $localDarkTheme and $darkTheme") },
                    )
                )

                // add help thing
                dropdownItems.add(
                    DDItem(
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                horizontalArrangement = Arrangement.spacedBy(LocalSpacings.current.m),
                            ) {
                                Text("Help", style = LocalTextStyles.current.m)
                            }
                        },
                        {
                            onboardingInitPage =
                                onboardingInitPageNum[outerNavPageNo * 10 + innerNavTabNo]
                            onboardingEndPage =
                                onboardingEndPageNum[outerNavPageNo * 10 + innerNavTabNo]
                            //System.out.println("RESHOWING ONBOARDING: $onboardingInitPage and $onboardingEndPage")
                            finishedOnboarding = false // re-show onboarding screen yey
                        },
                    )
                )


                // add replay onboarding screen fully
                dropdownItems.add(
                    DDItem(
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                horizontalArrangement = Arrangement.spacedBy(LocalSpacings.current.m),
                            ) {
                                Text("Features", style = LocalTextStyles.current.m)
                            }
                        },
                        {
                            onboardingInitPage = null
                            onboardingEndPage = null
                            //System.out.println("RESHOWING ONBOARDING: $onboardingInitPage and $onboardingEndPage")
                            finishedOnboarding = false // re-show onboarding screen yey
                        },
                    )
                )


                // init TTS
                initTTS()


            }



            //window.decorView.apply { systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN }

            /*var navBarVisible by remember { mutableStateOf((window.decorView.visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0)}

            window.decorView.setOnSystemUiVisibilityChangeListener {
                    if ((it and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                        navBarVisible = ((window.decorView.visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0)
                    }
                }*/

            var initHist by remember { mutableStateOf(true) }


            if (loaded) {

                ReadrTheme(darkTheme = localDarkTheme) {


                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        ScreenshotScope(
                            screenshotState = screenShotState,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colorScheme.background)
                        ) {


                            ProvideTextStyle(TextStyle(color = MaterialTheme.colorScheme.onBackground)) {
                                when (finishedOnboarding) {
                                    true -> ShowView(viewNo, {
                                        outerNavPageNo = it
                                        viewNo = it
                                    }, recomposeBool, ::recomposeOuter, firstOne, initHist, {initHist = it})

                                    false -> OnBoardingScreen(
                                        onboardingInitPage,
                                        onboardingEndPage,
                                        localDarkTheme,
                                    ) {
                                        finishedOnboarding = true
                                        Log.w("ONBOARDING", "FINISHED: $finishedOnboarding")
                                        val values = ContentValues()
                                        values.put(PersistentStorage.rdm, if (darkTheme) 1 else 0)
                                        contentResolver.insert(
                                            PersistentStorage.CONTENT_URI,
                                            values
                                        )
                                    }
                                }

                            }


                        }
                        screenShotState.bitmap.value?.asImageBitmap()?.let {
                            Image(
                                bitmap = it,
                                contentDescription = "screen shot",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(
                                        shape = RemovableExpandingRectShape(
                                            offsetX = animationOffsetX.value,
                                            offsetY = animationOffsetY.value,
                                        )
                                    )
                            )
                        }
                    }


                }

            } else {
                SplashScreen()
            }


            LaunchedEffect(true) {
                onboardingInitPage = 0
                onboardingEndPage = 4
            }


        }
    }

    var histItemNum = -1

    @OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
    @Composable
    fun ShowView(viewNo:Int, setViewNo:(Int)->Unit, recomposeBool: Boolean, recomposeOuter:()->Unit, firstOne:Boolean=false,
    initHist:Boolean, setInitHist:(Boolean)->Unit) {

        if (!firstOne) {
            LocalReplacedTextStyles.current.setOverlayTextSize(Variables.overlayTextSize)
            fh.saveOverlayTextSize()
        }

        LocalTextStyles.current.setTextScale(Variables.textScale) // TODO do we save this to firebase?


        var pagerState: PagerState = rememberPagerState(innerNavTabNo) { 3 }

        val pagerScope = rememberCoroutineScope()


        val topBarTitle by remember(pagerState.currentPage) { mutableStateOf(tab_titles[pagerState.currentPage]) }
        val topBarImg by remember(pagerState.currentPage) { mutableStateOf(topBarImgs[pagerState.currentPage]) }

        val initCameraOnback = {
            if (viewNo != 0) {
                innerNavTabNo = 1 // since it all must mean go back to dashboard yes
                setViewNo(0)
            }
        }

        val initHistItemMoveTextOnback = {
            if (viewNo == 4) {
                setViewNo(2)
            }
        }

        var onback = initCameraOnback
        BackPressHandler(onBackPressed = onback)

        val setOnback:((()->Unit)?)->Unit = {
            onback = if (it == null) {
                initCameraOnback
            }  else {
                it
            }
        }




        fun setIdx(newIdx:Int) {
            //pagerState.currentPage = newIdx
            innerNavTabNo = newIdx

            pagerScope.launch{ pagerState.animateScrollToPage(newIdx) }
        }


        when (viewNo) {
            0 -> Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                bottomBar = { key(recomposeBool) { BottomBar( pagerState.currentPage, { setIdx(it) ; should_listen = false ; speechRecognizer.stopListening() } , tab_titles , tab_images ) } },
                floatingActionButton = {
                    if (pagerState.currentPage==1) FloatingActionButton( onClick = {
                        setViewNo(1)
                        window.decorView.apply { systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN }
                                                                }, containerColor=MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.secondary )
                    { Image(painter=painterResource(R.drawable.camera_icon), contentDescription="Camera button",
                        modifier = Modifier.size(100.dp), colorFilter = ColorFilter.tint(if (darkTheme) Color.LightGray else Color.Black ))} },
            ) {
                val listState = rememberLazyListState()
                WrapInColllapsedTopBar(it, listState, dropdownItems, topBarTitle, true, Variables.textScale, recomposeBool) {

                    var toggle by remember { mutableStateOf(false) }

                    var readTxt by remember { mutableStateOf(readText) }
                    var currTxt by remember { mutableStateOf("") }

                    System.out.println("COMPOSING OUTERNAV 0")

                    Row(
                        modifier = Modifier.padding(top = COLLAPSED_TOP_BAR_HEIGHT + 16.dp,
                            bottom = 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                            ),
                        verticalAlignment = Alignment.Top,
                    ) {


                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                        ) {

                            if (pagerState.currentPage == 0) {

                                val microphonePermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

                                var currWord by remember { mutableStateOf(-1) } // listen for 0 1 and 2
                                ShowReadingViewPage(
                                    hasPermission = microphonePermissionState.status.isGranted,
                                    onRequestPermission = microphonePermissionState::launchPermissionRequest,
                                    toggle, { toggle = !toggle },
                                    readTxt, currTxt,
                                    { readTxt = it }, { currTxt = it },
                                    { recomposeOuter() }, currWord, {currWord = it},
                                    { newCurrTxt, newCurrWord -> currTxt=newCurrTxt; currWord=newCurrWord }
                                )

                            } else {

                                Column {

                                    LazyColumn(
                                        state = listState,
                                    ) {

                                        items(1) {
                                            when (pagerState.currentPage) {
                                                //0 ->
                                                1 -> ShowDashboard(
                                                    toggle,
                                                    { toggle = !toggle },
                                                    { recomposeOuter() })

                                                2 -> ShowSettings(
                                                    toggle,
                                                    { toggle = !toggle },
                                                    { recomposeOuter() },
                                                )
                                            }
                                        }


                                    }


                                    var histToggle by remember { mutableStateOf(false) }

                                    if (pagerState.currentPage == 1) {
                                        ShowHistory(histToggle,
                                            { histToggle = !histToggle ; System.out.println("RECOMPOSED IN FUNCTIOn") },
                                            { recomposeOuter() },
                                            { setViewNo(2) },
                                            { setViewNo(3) })

                                        if (initHist) {
                                            val initHistScope = rememberCoroutineScope()

                                            imgl.withNextImgNum({
                                                initHistScope.launch {
                                                    while (loadedHistItems != 2 * it) delay(100)
                                                    histToggle = !histToggle
                                                    setInitHist(false)
                                                }
                                            })

                                        }
                                        System.out.println("$histToggle")
                                    }

                                }

                            }

                        }


                    }


                }
            }

            1 -> {
                setOnback(initCameraOnback)
                val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

                ShowCameraPage(
                    hasPermission = cameraPermissionState.status.isGranted,
                    onRequestPermission = cameraPermissionState::launchPermissionRequest,
                    { setViewNo(0) },
                    { readText = it },
                    { setIdx(0) ; setViewNo(0) },
                    setOnback,
                    initCameraOnback,
                    dropdownItems,
                )
            }

            2 -> Scaffold( // SHOW HISTORY ITEM
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                topBar = { DisplayTopBar("History item $histItemNum", { setViewNo(0) }, dropdownItems) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .consumeWindowInsets(it)
                        .systemBarsPadding()
                        .noRippleClickable {
                            MainActivity.window.decorView.apply {
                                systemUiVisibility =
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
                            }; System.out.println("HIDING NAVBAR")
                        }
                    ,
                ) {
                    histItems[histItemNum].ShowDetails(recomposeOuter, { setViewNo(4) })
                }
            }

            3 -> Scaffold( // SHOW HISTORY PAGE
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                topBar = { DisplayTopBar("Text Replacement History", { setViewNo(0) }, dropdownItems) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .consumeWindowInsets(it)
                        .systemBarsPadding()
                        .noRippleClickable {
                            MainActivity.window.decorView.apply {
                                systemUiVisibility =
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
                            }; System.out.println("HIDING NAVBAR")
                        }
                    ,
                ) {
                    var histToggle by remember { mutableStateOf(false) }

                    ShowHistory(histToggle,
                            { histToggle = !histToggle ; System.out.println("RECOMPOSED IN FUNCTIOn") },
                            { recomposeOuter() },
                            { setViewNo(2) }, {})
                }
            }

            4 -> {
                setOnback(initHistItemMoveTextOnback)

                HistoryMoveTextScreen(histItems[histItemNum].init_bm!!, histItemNum, dropdownItems,
                ::sendNotification, {readText = it}, { setIdx(0) ; setViewNo(0) },
                    { setOnback { if (it != null) it{ initHistItemMoveTextOnback() } } },
                    { initHistItemMoveTextOnback() })
            }
        }
    }




    // for reading view stuff
    var readText = "text to read. \nThis is a placeholder. \nPlease use the camera feature, or edit this text by pressing the edit button below. "

    val wordSplitterRegex = Regex("""[\s\n]+""")
    val alphanumericRegex = Regex("""[a-zA-Z0-9]+""")
    val nonAlphaSpaceRegex = Regex("""[^a-zA-Z0-9\s\n]""")
    val nonAlphanumericRegex = Regex("""[^a-zA-Z0-9]""")

    /*fun getCurrWord(readTxt:String, currTxt:String): String {
        if (currTxt.length >= readTxt.length) {
            return ""
        } else {
            //System.out.println("readTxtSubstr: ${readTxt.substring(0, currTxt.length)}")
            if (readTxt.substring(0, currTxt.length) == currTxt) {
                val n = readTxt.substring(currTxt.length)
                return n.trim().split(wordSplitterRegex).first()
            } else {
                throw IllegalArgumentException("currTxt must be a subset of readTxt. (getCurrWord)")
            }
        }
    }*/

    var numWordsFuture = 3

    var words : List<String>? = null

    /*fun getDetectWords(readTxt:String, currTxt:String): List<String> {
        if (words == null) {
            // then initialize it
            words = readTxt.split(wordSplitterRegex)
        }

        if (currTxt.length >= readTxt.length) {
            return listOf()
        } else {
            if (readTxt.substring(0, currTxt.length) == currTxt) {
                val n = readTxt.substring(currTxt.length)
                val t = n.trim().split(wordSplitterRegex)
                val res =  t.subList(0, min(numWordsFuture, t.size))

                /*val res = mutableListOf<String>()
                for (ridx in rawres.indices) {
                    res.add(rawres[ridx].trim().lowercase())
                }*/

                System.out.print("Words possible: ")
                for (r in res) System.out.print("'$r', ")
                System.out.println()

                return res
            } else {
                throw IllegalArgumentException("currTxt must be a subset of readTxt.")
            }
        }
    }*/

    fun wordsMatch(word1:String, word2:String) : Boolean {
        val w1 = nonAlphanumericRegex.replace(word1, "").lowercase()
        val w2 = nonAlphanumericRegex.replace(word2, "").lowercase()
        //System.out.println("COMPARING: $w1 and $w2")
        return w1 == w2
    }

    fun getTillWord(readTxt:String, currTxt:String, w:String) : String {
        if (readTxt.substring(0, currTxt.length) == currTxt) {
            val n = readTxt.substring(currTxt.length)

            val word = w.split(nonAlphanumericRegex)[0]

            val res = n.substringBefore(word)

            if (res == word && (n.substring(0, word.length) != word)) {
                throw IllegalArgumentException("Word not found in readTxt after currTxt. ")
            } else {
                try {
                    val temp = currTxt + res + word + n.substringAfter(word,"").split(alphanumericRegex).first()
                    return temp
                } catch (e:Exception) {
                    return currTxt + res + word + ' '
                }
            }

        } else {
            throw IllegalArgumentException("currTxt must be a subset of readTxt. (getTillWord)")
        }
    }

    fun getNextWord(currWord:Int):String {
        if ((words==null) or (currWord+1 >= words!!.size)) return ""
        else return words!![currWord+1]
    }

    fun getDetectWords(currWord:Int):List<String> {
        val num = min(numWordsFuture, words!!.size-currWord-1)
        val res = mutableListOf<String>()
        for (i in 1..num) {
            res.add(words!![currWord+i])
        }
        return res
    }


    @Composable
    fun ShowReadingViewPage(hasPermission: Boolean, onRequestPermission: () -> Unit,
                            toggle:Boolean, toggleFunc:()->Unit,
                            readTxt:String, currTxt: String,
                            setReadTxt: (String)->Unit, setCurrTxt:(String)->Unit,
                            recomposeOuter:()->Unit, currWord:Int, setCurrWord: (Int) -> Unit,
                            setCurrs:(String, Int)->Unit) {

        if (hasPermission) {
            ShowReadingView(toggle, toggleFunc,
                readTxt, currTxt,
                setReadTxt, setCurrTxt,
                currWord, setCurrWord,
                setCurrs )

        } else {

            // request permission - adapted from https://github.com/YanneckReiss/JetpackComposeMLKitTutorial
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Please grant the permission to use the microphone to use the the reading feature of this app.", style= LocalTextStyles.current.l)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRequestPermission) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Microphone")
                    Text(text = "Grant permission", style=LocalTextStyles.current.xl)
                }
            }

        }

    }


    @Composable
    fun ShowReadingView(recomposeBool:Boolean, recompose:()->Unit,
                        readTxt:String, currTxt: String,
                        setReadTxt: (String)->Unit, setCurrTxt:(String)->Unit,
                        currWord:Int, setCurrWord:(Int)->Unit,
                        setCurrs: (String, Int) -> Unit ) {

        key(readTxt) {
            words = nonAlphaSpaceRegex.replace(readTxt, "").split(wordSplitterRegex)//.map{ w -> nonAlphanumericRegex.replace(w, "") }
            val temp = words!!.toMutableList()
            val iterator = temp.iterator()
            while (iterator.hasNext()) {
                val element = iterator.next()
                if (element.isEmpty()) {
                    iterator.remove()
                }
            }
            words = temp.toList()

            System.out.print("WORDS: ")
            for (w in words!!) {
                System.out.print("$w, ")
            }
            System.out.println("\ncurrWord: $currWord")
            System.out.println("READTXT LENGTH: ${readTxt.length} ending in: ${if (readTxt.length > 12) readTxt.substring(readTxt.length-10) else ""}")
            System.out.println("CURRTXT LENGTH: ${currTxt.length} ending in: ${if (currTxt.length > 12) currTxt.substring(currTxt.length-10) else ""}")
            System.out.println("NEXT WORD: ${getNextWord(currWord)}")
        }

        /*key(readTxt, currTxt) {
            if (currTxt.trim() == "") {
                words = readTxt.split(wordSplitterRegex)
                setCurrWord(-1) // means words done
            }
        }*/

        var showEditReadTxtDialog by remember { mutableStateOf(false) }


        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top,
        ) {
            ForceShowBottomColumn(
                modifier = Modifier.fillMaxSize(),
                spacingDP = 16
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                    ,
                ) {
                    if (readTxt.trim() != "") {
                        Text(readTxt, style = LocalTextStyles.current.m)

                        Text(
                            buildAnnotatedString {
                                withStyle(style = SpanStyle(color = LocalMoreColors.current.greyed_text)) {
                                    append(currTxt)
                                }

                                withStyle(style = SpanStyle(background = LocalMoreColors.current.highlight_text)) {
                                    append(
                                        getTillWord(
                                            readTxt,
                                            currTxt,
                                            getNextWord(currWord)
                                        ).substring(currTxt.length)
                                    )
                                }
                            },
                            style = LocalTextStyles.current.m,
                        )
                    } else {
                        Text(buildAnnotatedString {
                            withStyle(SpanStyle(color=Color(0xAAAAAAAA), fontWeight=FontWeight.Bold)) {
                                append("[NO TEXT FOUND]")
                            }
                        }, style=LocalTextStyles.current.xl)
                    }
                }

                // bottom menu of help buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .weight(0.12f)
                        .padding(16.dp)
                    ,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {

                    val initTextStyle = LocalTextStyles.current.l.copy()
                    var textStyle by remember { mutableStateOf(initTextStyle) }
                    var showTexts by remember { mutableStateOf(false) }

                    var textHeight by remember { mutableStateOf(100f) }

                    Button(enabled=(getNextWord(currWord)!=""), modifier = Modifier
                        .wrapContentSize()
                        .padding(end = 8.dp), onClick={
                        textToSpeech.stop()
                        textToSpeech.speak(getNextWord(currWord), TextToSpeech.QUEUE_FLUSH, null, "tts1")
                    }) {
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            Text(
                                "HELP",
                                modifier = Modifier.wrapContentWidth(),
                                style= textStyle,
                                maxLines=1,
                                softWrap=false,
                                onTextLayout = {
                                    textHeight=pxToDP(it.size.height)
                                    if (it.didOverflowWidth) {
                                        textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.9)
                                    } else {
                                        showTexts = true
                                    }
                                }
                            )

                            //AdaptiveText("HELP", LocalTextStyles.current.l, maxLines=1, softWrap=false,
                            //    modifier = Modifier.onGloballyPositioned { textHeight = pxToDP(it.size.height) }).ShowOverflowWidth()
                            Icon(painterResource(R.drawable.sound_icon), "Play word button", modifier=Modifier.height(textHeight.dp))
                        }
                    }

                    OutlinedButton({
                        showEditReadTxtDialog = true
                    }, shape=CircleShape, modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 8.dp)) {
                        Icon(Icons.Default.Edit, "Edit text to read",
                            tint = if (darkTheme) Color.Green
                            else Color.Blue)
                    }

                    Button(
                        enabled = (readTxt != currTxt),
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(start = 8.dp),
                        onClick = {
                        setCurrs(getTillWord(readTxt, currTxt, getNextWord(currWord)), currWord+1)
                            if (currWord+1 == words!!.size) recompose()
                    }) {
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            //AdaptiveText("SKIP", LocalTextStyles.current.l, maxLines=1, softWrap=false,
                            //    modifier = Modifier.onGloballyPositioned { textHeight = pxToDP(it.size.height) }).ShowOverflowWidth()

                            Text(
                                "SKIP",
                                modifier = Modifier.wrapContentWidth(),
                                style= textStyle,
                                maxLines=1,
                                softWrap=false,
                                onTextLayout = {
                                    textHeight=pxToDP(it.size.height)
                                    if (it.didOverflowWidth) {
                                        textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.9)
                                    } else {
                                        showTexts = true
                                    }
                                }
                            )

                            Icon(painterResource(R.drawable.skip_icon), "Skip word button", modifier=Modifier.height(textHeight.dp))
                        }
                    }
                }

            }
        }

        // show completed dialog if completed :)
        key(currWord) {
            System.out.println("WORDS SIZE: ${words!!.size} CURRWORD: $currWord")
            var showCompletedDialog by remember(
                words,
                currWord
            ) { mutableStateOf((words != null) and (words!!.size != 0) and (words!!.size == currWord + 1)) }
            ShowCompletedDialog(showCompletedDialog, {
                showCompletedDialog = false
                setCurrs("", -1)
            })
        }

        ShowEditReadTxtDialog(showEditReadTxtDialog, {showEditReadTxtDialog = !showEditReadTxtDialog}, readTxt, setReadTxt)


        // init STT
        var sttIntent = Intent()
        sttIntent =
            initSTT(onBeginSpeech = {
                // probably need nothing here so it's fine
            }, onResults = {
                if (innerNavTabNo == 0) speechRecognizer.startListening(sttIntent)
                // prob need nth??
            }, onPartialResults = {
                if (innerNavTabNo != 0) speechRecognizer.stopListening()

                if (it == null) {
                    Toast.makeText(this, "NO SPEECH DETECTED", Toast.LENGTH_SHORT).show()
                } else {
                    System.out.print("TEXT DETECTED FROM SPEECH: ")

                    for (s in it) {
                        System.out.println(s)
                        val word = s.split(wordSplitterRegex).last()

                        var w: String? = null
                        val detectWords = getDetectWords(currWord)
                        var dwidx = 0
                        while (dwidx < detectWords.size) {
                            //if (word.trim().lowercase() == detectWords[dwidx].trim().lowercase()) {
                            if (wordsMatch(word, detectWords[dwidx])) {
                                w = detectWords[dwidx]
                                break
                            }
                            dwidx++
                        }

                        if (w != null) {
                            System.out.println("MATCH FOUND!!")
                            setCurrs(getTillWord(readTxt, currTxt, w), currWord + dwidx+1)
                            //speechRecognizer.stopListening()
                            if ((words != null) and (words!!.size != 0) and (currWord+1 == words!!.size)) {
                                recompose()
                                return@initSTT
                            }
                        }


                    }

                    //Toast.makeText(this, "WORD DETECTED YAY", Toast.LENGTH_SHORT).show()
                    //Toast.makeText(this, "SPEECH DETECTED SUCCESSFULLY!", Toast.LENGTH_SHORT).show()
                    //Log.d("SPEECH DETECTOR", "SPEECH DETECTED SUCCESSFULLY")
                }
            }, restart = {
                if (innerNavTabNo == 0) speechRecognizer.startListening(sttIntent)
            })


        /*should_listen = false
        speechRecognizer.stopListening()
        speechRecognizer.startListening(sttIntent)
        should_listen = true*/

        LaunchedEffect(true) {
            speechRecognizer.startListening(sttIntent)
        }

    }

    val happyQuotes = listOf("“Happiness in intelligent people is the rarest thing I know.” ― Ernest Hemingway, The Garden of Eden",
            "“Attitude is a choice. Happiness is a choice. Optimism is a choice. Kindness is a choice. Giving is a choice. Respect is a choice. Whatever choice you make makes you. Choose wisely.” ― Roy T. Bennett, The Light in the Heart",
            "“Whoever is happy will make others happy.” ― Anne Frank, The Diary of a Young Girl",
            "“It isn't what you have or who you are or where you are or what you are doing that makes you happy or unhappy. It is what you think about it.” ― Dale Carnegie, How to Win Friends and Influence People",
            "“It's been my experience that you can nearly always enjoy things if you make up your mind firmly that you will.” ― Lucy Maud Montgomery, Anne of Green Gables",
            "“Happiness [is] only real when shared.” ― Jon Krakauer, Into the Wild",
            "“I must learn to be content with being happier than I deserve.” ― Jane Austen, Pride and Prejudice",
            "“Happiness is holding someone in your arms and knowing you hold the whole world.” ― Orhan Pamuk, Snow",
            "“The problem with people is they forget that most of the time it's the small things that count.” ― Jennifer Niven, All the Bright Places",
            "“Being happy isn't having everything in your life be perfect. Maybe it's about stringing together all the little things.” ― Ann Brashares, The Sisterhood of the Traveling Pants",
        )

    @Composable
    fun ShowCompletedDialog(showCompletedDialog: Boolean, reset: ()->Unit) {
        if (showCompletedDialog) {

            Toast.makeText(applicationContext, "SUCCESSFULLY FINISHED READING!", Toast.LENGTH_SHORT).show()

            mediaPlayer = MediaPlayer.create(applicationContext, R.raw.crowd_cheer)
            mediaPlayer!!.start()

            Dialog(onDismissRequest = {
                if (mediaPlayer != null) {
                    mediaPlayer!!.stop()
                }
                reset()
            }) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .noRippleClickable {
                            if (mediaPlayer != null) {
                                mediaPlayer!!.stop()
                            }
                            reset()
                        }
                ) {
                    val dm: DisplayMetrics = MainActivity.context.resources.displayMetrics

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = COLLAPSED_TOP_BAR_HEIGHT)
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            Spacer(Modifier.weight(0.5f))
                            Confetti(0f, 0f, Modifier.weight(0.1f) )
                            Spacer(Modifier.weight(0.5f))
                            Confetti(0f, 0f, Modifier.weight(0.1f) )
                            Spacer(Modifier.weight(0.5f))
                        }

                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 32.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            Spacer(Modifier.weight(0.5f))
                            Fountain(0f, (dm.heightPixels*0.8).toFloat(), Modifier.weight(0.1f) )
                            Spacer(Modifier.weight(0.8f))
                            Fountain(0f, (dm.heightPixels*0.8).toFloat(), Modifier.weight(0.1f) )
                            Spacer(Modifier.weight(0.8f))
                            Fountain(0f, (dm.heightPixels*0.8).toFloat(), Modifier.weight(0.1f) )
                            Spacer(Modifier.weight(0.5f))
                        }

                    }
                }


                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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

                        Text("GOOD JOB!!", modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style= LocalTextStyles.current.l)

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(happyQuotes[nextInt(0, happyQuotes.size)], modifier = Modifier.padding(16.dp),
                            style = LocalTextStyles.current.m)

                    }
                }

            }



        }
    }



    @Composable
    fun ShowEditReadTxtDialog(show:Boolean, hide:()->Unit, readTxt:String, setReadTxt: (String) -> Unit) {
        if (show) {
            Dialog(onDismissRequest = {
                hide()
            }) {

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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

                        var txt by remember { mutableStateOf(readTxt) }


                        ForceShowBottomColumn(modifier = Modifier.wrapContentSize(), spacingDP=20, useMaxHeight=false) {

                            Box(modifier = Modifier
                                .wrapContentSize()
                                .padding(16.dp)) {
                                TextField(
                                    txt,
                                    { txt = it },
                                    textStyle = LocalTextStyles.current.m,
                                    modifier = Modifier
                                        .heightIn(max = 200.dp)
                                        .horizontalScroll(rememberScrollState())
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.End,
                            ) {
                                Button(modifier = Modifier.wrapContentHeight(), onClick={
                                    setReadTxt(txt)
                                    hide()
                                }) {
                                    AdaptiveText("Confirm", LocalTextStyles.current.m, 1, false).ShowOverflow()
                                }
                            }
                        }



                    }
                }

            }
        }
    }


    @Composable
    fun ShowDashboard(recomposeBool:Boolean, recompose:()->Unit, recomposeOuter:()->Unit, ) {

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ChangeReplacedTextSizeSlider(Variables.overlayTextSize) {
                Variables.overlayTextSize = it
                recomposeOuter()
            }

            Button({
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            },
                Modifier.wrapContentSize(),
            ) {
                AdaptiveText("Open Settings accessibility page", LocalTextStyles.current.m, maxLines=1, softWrap=false).ShowOverflowWidth()
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()


        }


    }

    @Composable
    fun ShowHistory(recomposeBool:Boolean, recompose:()->Unit, recomposeOuter: () -> Unit, showHistoryItemView: () -> Unit, goToHistoryPage: ()->Unit ) {

        Spacer(modifier = Modifier.height(16.dp))

        var showConfirmationDialog by remember { mutableStateOf(false) }

        ForceShowRightRow(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .forceRecomposeWith(showConfirmationDialog),
            //horizontalArrangement = Arrangement.SpaceBetween,
            //verticalAlignment = Alignment.CenterVertically,
            spacingDP = 10,
        ) {
            AdaptiveText("Text Replacement History", LocalTextStyles.current.xl, maxLines=1, softWrap=false, Modifier.padding(end=12.dp)).ShowOverflowWidth()

            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                var currentRotation by remember { mutableStateOf(0f) }
                val rotation = remember { Animatable(currentRotation) }

                val rotationScope = rememberCoroutineScope()
                Icon(
                    Icons.Filled.Refresh, "Refresh history",
                    tint = Color(0x99999999),
                    modifier = Modifier
                        .size(32.dp)
                        .forceRecomposeWith(recomposeBool)
                        .rotate(rotation.value)
                        .noRippleClickable {
                            rotationScope.launch {
                                rotation.animateTo(
                                    targetValue = currentRotation + 360f,
                                    animationSpec = tween(
                                        durationMillis = 750,
                                        easing = LinearOutSlowInEasing
                                    )
                                )

                                rotation.snapTo(0f)

                                rotation.animateTo(
                                    targetValue = currentRotation + 360f,
                                    animationSpec = tween(
                                        durationMillis = 750,
                                        easing = LinearOutSlowInEasing
                                    )
                                )

                                rotation.snapTo(0f)

                            }

                            this@MainActivity.loadHistItems { System.out.println("LOADED YAY"); recompose() }
                        }
                )


                Icon(
                    Icons.Filled.Delete, "Delete/Clear Text replacement history",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(32.dp)
                        .noRippleClickable {
                            // confirmation dialog
                            showConfirmationDialog = true
                            System.out.println("CONFIMRATIONDIALOG")
                            recompose()
                        },
                )


                Icon(painterResource(R.drawable.history_icon), "Text replacement history",
                modifier = Modifier
                    .size(32.dp)
                    .forceRecomposeWith(recomposeBool)
                    .noRippleClickable(goToHistoryPage)
                    , tint =
                    if (darkTheme) Color.Green
                    else Color.Blue
                )


            }

        }

        Spacer(modifier = Modifier.height(16.dp))


        key(histItems) {


            System.out.println("BOXING YES $recomposeBool")

            LazyVerticalGrid(
                GridCells.Adaptive(minSize = HistoryItem.width),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
            ) {

                items(histItems.size) {
                    histItems[histItems.size - it - 1].ShowView({
                        histItemNum = histItems.size - it - 1
                        showHistoryItemView()
                    }, recompose) // so that highest num, latest, is shown first
                }
            }

        }




        // show confirmation dialog if yes
        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = {
                    showConfirmationDialog = false
                    System.out.println("IN CONFIRMATIONDIALOG")
                    recompose()
                },
                title = {
                    Text("Clear history: CONFIRMATION", style=LocalTextStyles.current.l)
                },
                text = {
                    Text("Are you sure you want to clear all history? ", style= LocalTextStyles.current.m)
                },
                confirmButton = {
                    Button({
                        imgl.deleteAllImages {
                            histItems.clear()
                            showConfirmationDialog = false
                            recomposeOuter()
                            System.out.println("IN CONFIRMATIONDIALOG CONFIRMED")
                            recompose()
                        }
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                        )
                    ) {
                        Text("Confirm", style= LocalTextStyles.current.l)
                    }
                },
                dismissButton = {
                    Button({
                        showConfirmationDialog = false
                        System.out.println("IN CONFIRMATIONDIALOG DISMISSBUTTON")
                        recompose()
                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                        )
                    ) {
                        Text("Cancel", style= LocalTextStyles.current.l)
                    }
                }
            )
        }
    }




    @Composable
    fun ShowSettings(recomposeBool:Boolean, recompose:()->Unit, recomposeOuter:()->Unit, ) {

        var textScale by remember { mutableFloatStateOf(Variables.textScale) }

        Column (
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            var extra by remember { mutableStateOf(1) }

            ChangeReplacedTextSizeSlider(Variables.overlayTextSize, textScale, extra, {
                Variables.overlayTextSize = it
                recomposeOuter()
            })

            ChangeTextScaleSlider(textScale, {
                Variables.textScale = it
                textScale = it
                recomposeOuter()
                recompose()
            }, extra, {extra++})


            ChangeFontFamilyItem(if (Variables.textFontFamily== openDyslexic) 0 else 1,
                { fontNum:Int ->
                    if (fontNum==0) {
                        Variables.textFontFamily = openDyslexic
                        Variables.overlayFontFamily = openDyslexic
                    } else if (fontNum == 1) {
                        Variables.textFontFamily = FontFamily.SansSerif
                        Variables.overlayFontFamily = FontFamily.SansSerif
                    }
                    recomposeOuter()
                    recompose()
                    },
                extra, {extra++} )
        }

    }




    @Composable
    fun ShowCameraPage(
        hasPermission: Boolean,
        onRequestPermission: () -> Unit,
        backButtonFunc: () -> Unit,
        setReadText: (String) -> Unit,
        goToReadTextScreen: () -> Unit,
        setOnback: ((() -> Unit)?) -> Unit,
        initOnback: ()->Unit,
        dropdownItems: MutableList<DDItem>,
    ) {
        if (hasPermission) {
            var camera by remember { mutableStateOf(true) }

            CameraScreen(backButtonFunc, ::sendNotification,
                setReadText, goToReadTextScreen, setOnback, initOnback,
                dropdownItems, camera, { camera = it })
        } else {

            // request permission - adapted from https://github.com/YanneckReiss/JetpackComposeMLKitTutorial
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Please grant the permission to use the camera to use the the camera feature of this app.", style= LocalTextStyles.current.l)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRequestPermission) {
                    Icon(imageVector = Icons.Default.Camera, contentDescription = "Camera")
                    Text(text = "Grant permission", style=LocalTextStyles.current.xl)
                }
            }

        }
    }




    /*fun stopAccessibilityMenu() {
        val intent = Intent(this@MainActivity, AccessibilityMenuService::class.java)
        stopService(intent)
        mBounded = false
        Toast.makeText(this, "CLOSED ACCESSIBILITY MENU", Toast.LENGTH_SHORT).show()
    }*/


    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        should_listen = false
        speechRecognizer.stopListening()

        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
        }
    }

}

