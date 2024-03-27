package com.example.readr

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.torrydo.floatingbubbleview.CloseBubbleBehavior
import com.torrydo.floatingbubbleview.service.expandable.BubbleBuilder
import com.torrydo.floatingbubbleview.service.expandable.ExpandableBubbleService
import com.torrydo.floatingbubbleview.service.expandable.ExpandedBubbleBuilder
import io.grpc.Server


class AccessibilityMenuService: ExpandableBubbleService() {


    var mBinder: IBinder = LocalBinder(this)

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    class LocalBinder(val instance:AccessibilityMenuService) : Binder() {}


    override fun onCreate() {
        super.onCreate()
        minimize()
    }

    // normal bubble will be just button, expanded will have more.

    // optional, only required if you want to call minimize()
    override fun configBubble(): BubbleBuilder? {
        return BubbleBuilder(this)
            .bubbleCompose {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .sizeIn(maxHeight = 100.dp, maxWidth = 100.dp)
                        .padding(8.dp),
                ) {
                    Text("TEST YAY")

                    Button({ expand() }) {
                        Text("EXPAND BUBBLE")
                    }

                }

            }
            .startLocation(100, 100)
            .enableAnimateToEdge(true)
            .distanceToClose(100)
            .closeBehavior(CloseBubbleBehavior.FIXED_CLOSE_BUBBLE)
            .distanceToClose(1000)
    }


    // optional, only required if you want to call expand()
    override fun configExpandedBubble(): ExpandedBubbleBuilder? {
        return ExpandedBubbleBuilder(this)
            .expandedCompose {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .sizeIn(maxHeight = 300.dp, maxWidth = 300.dp)
                        .padding(8.dp)
                ) {
                    Text("EXPANDED TEST YAY")
                    Button({ minimize() }) {
                        Text("CLOSE")
                    }

                    Box {
                        Text("asdffdsa", modifier = Modifier
                            .offset(x = 100.dp, y = 100.dp)
                            .rotate(22.5F))
                    }
                }

                Box {
                    Text("asdf", modifier = Modifier
                        .offset(x = 100.dp, y = 100.dp)
                        .rotate(22.5F))
                }

            }
    }
}