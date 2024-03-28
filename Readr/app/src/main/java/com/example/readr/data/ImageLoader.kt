package com.example.readr.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ImageLoader {

    var storageRef = FirebaseStorage.getInstance().reference


    fun withNextImgNum(f:(Int)->Unit) {
        storageRef.listAll().addOnSuccessListener {
            f(it.items.size/2)
        }
    }


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
                //Log.w("HSIHFEOKFESD", "SUCCESS: $it")
                onSuccess(ByteArrayToBitmap(it).asImageBitmap())
            }
        }


    }

    fun saveImage(bitmap:Bitmap, name:String, ctx: Context) { // ALL .PNG
        val ref = storageRef.child(name)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

        ref.putBytes(baos.toByteArray()).addOnFailureListener {
            Log.e("FILE UPLOAD FAIL", "FAILED TO UPLOAD FILE $name TO FIREBVASE STORAGE", it)
            Toast.makeText(ctx, "FAILED TO UPLOAD FILE $name TO FIREBASE STORAGE", Toast.LENGTH_SHORT).show()
        }
    }


    fun deleteAllImages() {
        storageRef.listAll().addOnSuccessListener {
            for (item in it.items) {
                item.delete()
            }
        }
    }


}
