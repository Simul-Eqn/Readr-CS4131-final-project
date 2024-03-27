package com.example.readr.backup

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent

// https://developer.android.com/guide/topics/ui/accessibility/service

class DetectorAccessibilityMenu : AccessibilityService() {

    private var mAccessibilityButtonController: AccessibilityButtonController? = null
    private var accessibilityButtonCallback:
            AccessibilityButtonController.AccessibilityButtonCallback? = null
    private var mIsAccessibilityButtonAvailable: Boolean = false

    override fun onServiceConnected() {
        mAccessibilityButtonController = accessibilityButtonController
        mIsAccessibilityButtonAvailable =
            mAccessibilityButtonController?.isAccessibilityButtonAvailable ?: false

        if (!mIsAccessibilityButtonAvailable) return

        serviceInfo = serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_ACCESSIBILITY_BUTTON
        }

        accessibilityButtonCallback =
            object : AccessibilityButtonController.AccessibilityButtonCallback() {
                override fun onClicked(controller: AccessibilityButtonController) {
                    Log.d("DETECTOR_ACCESSIBILITY_MENU_TAG", "Accessibility button pressed!")

                    // TODO: RUN THE SERVICE HERE
                }

                override fun onAvailabilityChanged(
                    controller: AccessibilityButtonController,
                    available: Boolean
                ) {
                    if (controller == mAccessibilityButtonController) {
                        mIsAccessibilityButtonAvailable = available
                    }
                }
            }

        accessibilityButtonCallback?.also {
            mAccessibilityButtonController?.registerAccessibilityButtonCallback(it)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        TODO("Not yet implemented")
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }


}