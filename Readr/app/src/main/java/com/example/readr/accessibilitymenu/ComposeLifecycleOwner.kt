package com.example.readr.accessibilitymenu


import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


internal class ComposeLifecycleOwner : SavedStateRegistryOwner, ViewModelStoreOwner {

    private var _view: View? = null
    private var recomposer: Recomposer? = null
    private var runRecomposeScope: CoroutineScope? = null
    private var coroutineContext: CoroutineContext? = null

    init {
        coroutineContext = AndroidUiDispatcher.CurrentThread

    }

    fun onCreate() {

        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        runRecomposeScope?.cancel()

        runRecomposeScope = CoroutineScope(coroutineContext!!)
        recomposer = Recomposer(coroutineContext!!)
        _view?.compositionContext = recomposer

        runRecomposeScope!!.launch {
            recomposer!!.runRecomposeAndApplyChanges()
        }


    }

    fun onStart() {
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        } catch (e: Exception) {
            /**
             * java_vm_ext.cc:594] JNI DETECTED ERROR IN APPLICATION: JNI CallVoidMethodV called with pending exception java.lang.IllegalStateException: no event up from DESTROYED
             * java_vm_ext.cc:594] at void androidx.lifecycle.LifecycleRegistry.forwardPass(androidx.lifecycle.LifecycleOwner) (LifecycleRegistry.java:269)
             *
             * try catch then the issue fixed, somehow... =)))
             * */
//            e.printStackTrace()
        }
    }

    fun onResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onPause() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    fun onStop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        savedStateRegistryController.performSave(Bundle())
//        runRecomposeScope?.cancel()
    }

    /**
     * Compose uses the Window's decor view to locate the
     * Lifecycle/ViewModel/SavedStateRegistry owners.
     * Therefore, we need to set this class as the "owner" for the decor view.
     */
    fun attachToDecorView(view: View?) {
        if (view == null) return

        _view = view

//        ViewTreeLifecycleOwner.set(view, this)
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)

    }

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
//    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry


    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store
}




// old attempts


internal class MyLifecycleOwner : SavedStateRegistryOwner {

    override var savedStateRegistry: SavedStateRegistry
    override var lifecycle: LifecycleRegistry = LifecycleRegistry(this)
    private var mSavedStateRegistryController: SavedStateRegistryController = SavedStateRegistryController.create(this)

    val isInitialized: Boolean
        get() = true

    init {
        savedStateRegistry = mSavedStateRegistryController.savedStateRegistry
    }


    fun setCurrentState(state: Lifecycle.State) {
        lifecycle.currentState = state
    }

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycle.handleLifecycleEvent(event)
    }


    fun performRestore(savedState: Bundle?) {
        mSavedStateRegistryController.performRestore(savedState)
    }

    fun performSave(outBundle: Bundle) {
        mSavedStateRegistryController.performSave(outBundle)
    }
}

internal class MyLifecycleOwner2 :
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    fun onResume() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onPause() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    fun onDestroy() {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
    }

    // LifecycleOwner methods
    override val lifecycle: LifecycleRegistry = LifecycleRegistry(this)

    // ViewModelStore methods
    override val viewModelStore = ViewModelStore()

    override var savedStateRegistry: SavedStateRegistry

    // SavedStateRegistry methods
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    init {
        savedStateRegistry = savedStateRegistryController.savedStateRegistry
    }
}



