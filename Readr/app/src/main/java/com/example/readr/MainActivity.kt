package com.example.readr

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
//import com.example.readr.AccessibilityMenuService.LocalBinder
import com.example.readr.data.PersistentStorage
import com.example.readr.presentation.ChangeReplacedTextSizeSlider
import com.example.readr.presentation.onboarding.OnBoardingScreen
import com.example.readr.presentation.onscaffold.BottomBar
import com.example.readr.presentation.onscaffold.COLLAPSED_TOP_BAR_HEIGHT
import com.example.readr.presentation.onscaffold.DDItem
import com.example.readr.presentation.onscaffold.ExpandedTopBar
import com.example.readr.presentation.onscaffold.WrapInColllapsedTopBar
import com.example.readr.presentation.themeswitcher.ThemeSwitcher
import com.example.readr.ui.theme.LocalMoreColors
import com.example.readr.ui.theme.LocalSpacings
import com.example.readr.ui.theme.LocalTextStyles
import com.example.readr.ui.theme.ReadrTheme
import java.util.Locale
import kotlin.math.min


class MainActivity : ComponentActivity() {

    // in main view, 0. other outernavigation things do not have tabs (?)
    val tab_titles:MutableList<String> = mutableListOf()
    val tab_images:MutableList<Any> = mutableListOf()
    var topBarImgs:MutableList<Any> = mutableListOf()

    var dropdownItems:MutableList<DDItem> = mutableListOf()

    var outerNavPageNo:Int = 0
    var innerNavTabNo:Int = 1

    // TTS
    lateinit var textToSpeech: TextToSpeech

    // STT
    private lateinit var speechRecognizer: SpeechRecognizer

    // ACCESSIBILITY MENU
    /*var mBounded = false
    var mAMenu:AccessibilityMenuService? = null
    var mConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            Toast.makeText(this@MainActivity, "Service is disconnected", Toast.LENGTH_SHORT).show()
            mBounded = false
            mAMenu = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Toast.makeText(this@MainActivity, "Service is connected", Toast.LENGTH_SHORT).show()
            mBounded = true
            val mLocalBinder = service as LocalBinder
            mAMenu = mLocalBinder.instance
        }
    }*/


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
                onPartialResults: (ArrayList<String>?) -> Unit ): Intent {
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        sttIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() { onBeginSpeech() }
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(i: Int) {}
            override fun onResults(bundle: Bundle) {
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                onResults(data)
                speechRecognizer.startListening(sttIntent)
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

            // init TTS
            initTTS()


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
                WrapInColllapsedTopBar(it, listState, dropdownItems, topBarTitle, true) {

                    var toggle by remember { mutableStateOf(false) }

                    var readTxt by remember { mutableStateOf(readText) }
                    var currTxt by remember { mutableStateOf("") }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.padding(top = COLLAPSED_TOP_BAR_HEIGHT),
                    ) {
                        /* if (outerNavPageNo == 0 && innerNavTabNo == 1) {
                            item() {
                                ExpandedTopBar(topBarImg, topBarTitle)
                            }
                        } */ // no more expanded top bar


                        items(1) {
                            when (idx) {
                                0 -> ShowReadingView(toggle, { toggle = !toggle },
                                    readTxt, currTxt,
                                    { readTxt = it }, { currTxt = it })
                                1 -> ShowDashboard(toggle, { toggle = !toggle })
                                2 -> ShowSettings(toggle, { toggle = !toggle })
                            }
                        }

                    }
                }
            }
        }
    }

    var readText = "text to read. Please work haha. "

    fun getCurrWord(readTxt:String, currTxt:String): String {
        System.out.println("CHECKING CURR WORD")
        System.out.println("readTxt: $readTxt")
        System.out.println("currTxt: $currTxt")
        if (currTxt.length >= readTxt.length) {
            return ""
        } else {
            if (readTxt.substring(0, currTxt.length) == currTxt) {
                val n = readTxt.substring(currTxt.length)
                return n.trim().substringBefore(' ')
            } else {
                throw IllegalArgumentException("currTxt must be a subset of readTxt.")
            }
        }
    }

    var numWordsFuture = 3

    fun getDetectWords(readTxt:String, currTxt:String): List<String> {
        if (currTxt.length <= readTxt.length) {
            return listOf()
        } else {
            if (readTxt.substring(0, currTxt.length) == currTxt) {
                val n = readTxt.substring(currTxt.length)
                val t = n.trim().split(' ')
                return t.subList(0, min(numWordsFuture, t.size))
            } else {
                throw IllegalArgumentException("currTxt must be a subset of readTxt.")
            }
        }
    }

    fun getTillWord(readTxt:String, currTxt:String, word:String) : String {
        if (readTxt.substring(0, currTxt.length) == currTxt) {
            val n = readTxt.substring(currTxt.length)

            /* regex attempt - probably not needed, though
            var matchRes = Regex.fromLiteral(word).find(n)
            if (matchRes == null) {
                throw IllegalArgumentException("Word not found in readTxt after currTxt.")
            } else {
                return readTxt + n.substring(0, matchRes.range.last)
            }
            */
            System.out.println("FINDING WORD $word IN readTxt: \n$readTxt\n; currTxt: \n$currTxt\n; n: \n$n\n; nsubstr: \n${n.substring(0, word.length)}\n\n")

            val res = n.substringBefore(word, "")

            if (res == "" && (n.substring(0, word.length) != word)) {
                throw IllegalArgumentException("Word not found in readTxt after currTxt. ")
            } else {
                return currTxt + res + word + ' '
            }

        } else {
            throw IllegalArgumentException("currTxt must be a subset of readTxt.")
        }
    }

    @Composable
    fun ShowReadingView(recomposeBool:Boolean, recompose:()->Unit,
                        readTxt:String, currTxt: String,
                        setReadTxt: (String)->Unit, setCurrTxt:(String)->Unit) {
        Text("READING VIEW")


        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {

                Text("FOCUSED READING OUT LOUD MODE", style = LocalTextStyles.current.xl)

                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(readTxt, style=LocalTextStyles.current.m)

                    Text(
                        buildAnnotatedString {
                            withStyle(style=SpanStyle(color=LocalMoreColors.current.greyed_text)) {
                                append(currTxt)
                            }

                            withStyle(style=SpanStyle(background=LocalMoreColors.current.highlight_text)) {
                                append(getTillWord(readTxt, currTxt, getCurrWord(readTxt, currTxt)).substring(currTxt.length))
                            }
                        },
                        style=LocalTextStyles.current.m,
                    )
                }

                // bottom menu of help buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Button({
                        textToSpeech.stop()
                        textToSpeech.speak(getCurrWord(readTxt, currTxt), TextToSpeech.QUEUE_FLUSH, null, "tts1")
                    }) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("HELP")
                            Icon(painterResource(R.drawable.sound_icon), "Play word button", modifier=Modifier.heightIn(30.dp, 30.dp))
                        }
                    }

                    Button(
                        enabled = (readTxt != currTxt),
                        onClick = {
                        setCurrTxt(getTillWord(readTxt, currTxt, getCurrWord(readTxt, currTxt)))
                    }) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("SKIP")
                            Icon(painterResource(R.drawable.skip_icon), "Skip word button", modifier=Modifier.heightIn(30.dp, 30.dp))
                        }
                    }
                }

            }
        }

        // show completed dialog if completed :)
        var showCompletedDialog by remember(readTxt, currTxt) { mutableStateOf(readTxt == currTxt) }
        ShowCompletedDialog(showCompletedDialog, {
            showCompletedDialog = false
            setCurrTxt("")
        })


        // init STT
        val sttIntent = remember {
            initSTT(onBeginSpeech = {
                // probably need nothing here so it's fine
            }, onResults = {
                // prob need nth here
            }, onPartialResults = {
                if (it == null) {
                    Toast.makeText(this, "NO SPEECH DETECTED", Toast.LENGTH_SHORT).show()
                } else {
                    System.out.println("TEXT DETECTED FROM SPEECH: ")

                    for (s in it) {
                        System.out.println(s)
                        for (w in s.split(' ')) {
                            if (w in getDetectWords(readTxt, currTxt)) {
                                setCurrTxt(getTillWord(readTxt, currTxt, w))
                                if (currTxt == readTxt) {

                                    return@initSTT
                                }
                            }
                        }
                    }

                    //Toast.makeText(this, "SPEECH DETECTED SUCCESSFULLY!", Toast.LENGTH_SHORT).show()
                    Log.d("SPEECH DETECTOR", "SPEECH DETECTED SUCCESSFULLY")
                }
            })
        }

        speechRecognizer.startListening(sttIntent)

    }

    @Composable
    fun ShowCompletedDialog(showCompletedDialog: Boolean, reset: ()->Unit) {
        if (showCompletedDialog) {

            Toast.makeText(applicationContext, "SUCCESSFULLY FINISHED READING!", Toast.LENGTH_SHORT).show()

            Dialog(onDismissRequest = {
                reset()
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

                    Text("YAY YOU COMPLETED HAHA", modifier = Modifier.padding(10.dp))
                    // TODO: better completion screen to give sense of accomplishment

                }

            }
        }
    }


    @Composable
    fun ShowDashboard(recomposeBool:Boolean, recompose:()->Unit) {
        Text("DASHBOARD VIEW")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ChangeReplacedTextSizeSlider()

            var amenuEnabled: Boolean by remember(
                (getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager) // accessibilty manager
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK) // list of feedback
            ) {
                mutableStateOf(
                    (getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager) // accessibilty manager
                        .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK) // list of feedback
                        .map{ it.resolveInfo.serviceInfo.packageName.equals("com.example.readr.accessibilitymenu") } // check if any are yes
                        .any() // boolean
                )
            }

            // tried power button, but requires rooted phone.
            /*Button({

                // check if already enabled
                if (!amenuEnabled) {

                    Settings.Secure.putString(
                        contentResolver,
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                        "com.example.readr.accessibilitymenu/AccessibilityMenu"
                    )
                    Settings.Secure.putString(
                        contentResolver,
                        Settings.Secure.ACCESSIBILITY_ENABLED, "1"
                    )

                } else {
                    // disable
                    Settings.Secure.dele
                }

                /*if (!mBounded) {
                    if (!Settings.canDrawOverlays(application)) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivityForResult(intent, 0)

                    } else {
                        val intent = Intent(this@MainActivity, AccessibilityMenuService::class.java)
                        startForegroundService(intent)
                        bindService(intent, mConnection, BIND_AUTO_CREATE)
                    }
                } else {
                    Toast.makeText(applicationContext, "REMOVING YES", Toast.LENGTH_SHORT).show()
                    stopAccessibilityMenu()
                }*/

            },
                modifier= Modifier.size(100.dp),  //avoid the oval shape
                shape = CircleShape,
                border= BorderStroke(1.dp, Color.Blue),
                contentPadding = PaddingValues(0.dp),  //avoid the little icon
                colors = ButtonDefaults.outlinedButtonColors(contentColor =  Color.Blue)
            ) {
                Icon(painterResource(R.drawable.power_icon), "Launch/Close Accessibility Menu", modifier = Modifier.heightIn(70.dp, 70.dp))
            }*/

            Button({
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            },
                Modifier.wrapContentSize(),
            ) {
                Text("Open accessibility page in Settings")
            }


        }


    }

    @Composable
    fun ShowSettings(recomposeBool:Boolean, recompose:()->Unit) {
        Text("SETTINGS VIEW")

        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ChangeReplacedTextSizeSlider()
        }

    }


    override fun onStop() {
        super.onStop()
    }

    /*fun stopAccessibilityMenu() {
        val intent = Intent(this@MainActivity, AccessibilityMenuService::class.java)
        stopService(intent)
        mBounded = false
        Toast.makeText(this, "CLOSED ACCESSIBILITY MENU", Toast.LENGTH_SHORT).show()
    }*/

}

