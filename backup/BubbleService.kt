package com.example.readr.backup

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.torrydo.floatingbubbleview.service.expandable.BubbleBuilder
import com.torrydo.floatingbubbleview.service.expandable.ExpandableBubbleService
import com.torrydo.floatingbubbleview.service.expandable.ExpandedBubbleBuilder

class BubbleService : ExpandableBubbleService() {

    lateinit var intent: Intent

    override fun onCreate() {
        super.onCreate()
        expand()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.intent = intent!!
        return super.onStartCommand(intent, flags, startId)
    }

    override fun configBubble(): BubbleBuilder? {
        return null
    }


    override fun configExpandedBubble(): ExpandedBubbleBuilder {

        return ExpandedBubbleBuilder(this)
            .expandedCompose {
                Log.d("BUBBLESERVICE", "YAY INTENT: $intent")
                // TODO
                Text("HIIII")

                Button({ removeAll() }) {
                    Text("CLOSE")
                }
            }
            // set start location in dp
            .startLocation(0, 0)
            // allow expanded bubble can be draggable or not
            .draggable(false)
            // fade animation by default
            .style(null)
            .fillMaxWidth(true)
    }


}