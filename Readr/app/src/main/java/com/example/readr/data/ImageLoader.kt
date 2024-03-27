package com.example.readr.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.firebase.storage.FirebaseStorage
import java.io.InputStream

class ImageLoader {

    var storageRef = FirebaseStorage.getInstance().reference


    fun ByteArrayToBitmap(imageData: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
    }


    fun LoadImageBytes(name:String, onSuccess:(ImageBitmap)->Unit) {

        if (name.subSequence(0, 4) == "http") {
            //System.out.println("LOAD AS URL")
            // it's a url
            Thread {
                try {
                    val ins: InputStream =
                        java.net.URL(name)
                            .openConnection().getInputStream()
                    onSuccess(BitmapFactory.decodeStream(ins).asImageBitmap()) // returns this value mm.
                } catch (e: Exception) {
                    //Log.e("Error", e.message!!);
                    e.printStackTrace()
                }
            }.start()

        } else {
            //System.out.println("LOAD FROM FIREBASE STORAGE")

            // load from db
            val ref = storageRef.child(name).getBytes(Long.MAX_VALUE).addOnSuccessListener {
                Log.w("HSIHFEOKFESD", "SUCCESS: $it")
                onSuccess(ByteArrayToBitmap(it).asImageBitmap())
            }
        }


    }


}
