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
import com.example.readr.MainActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ImageLoader {

    var storageRef = FirebaseStorage.getInstance().reference


    fun withNextImgNum(f:(Int)->Unit, onFailure: (Exception) -> Unit={}) {
        try {
            storageRef.listAll().addOnSuccessListener {
                System.out.println("NUMBER OF ITEMS: ${it.items.size/2}")
                f(it.items.size / 2)
            }.addOnFailureListener { onFailure(it) }
        } catch (e:Exception) {
            onFailure(e)
        }
    }


    fun ByteArrayToBitmap(imageData: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
    }


    fun LoadImageBytes(name:String, onSuccess:(ImageBitmap)->Unit, onFailure:(Exception)->Unit={}) {

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
                onSuccess(ByteArrayToBitmap(it).copy(Bitmap.Config.RGBA_F16, true).asImageBitmap())
            } .addOnFailureListener {
                onFailure(it)
            }
        }


    }

    fun saveImage(bitmap:Bitmap, name:String, ctx: Context = MainActivity.context) { // ALL .PNG
        val ref = storageRef.child(name)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)

        ref.putBytes(baos.toByteArray()).addOnFailureListener {
            Log.e("FILE UPLOAD FAIL", "FAILED TO UPLOAD FILE $name TO FIREBVASE STORAGE", it)
            Toast.makeText(ctx, "FAILED TO UPLOAD FILE $name TO FIREBASE STORAGE", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveImage(byteArray:ByteArray, name:String, ctx:Context = MainActivity.context) { // ALSO .PNG
        storageRef.child(name).putBytes(byteArray).addOnFailureListener {
            Log.e("FILE UPLOAD FAIL", "FAILED TO UPLOAD FILE $name TO FIREBVASE STORAGE", it)
            Toast.makeText(ctx, "FAILED TO UPLOAD FILE $name TO FIREBASE STORAGE", Toast.LENGTH_SHORT).show()
        }
    }


    fun deleteAllImages(onComplete:()->Unit={}) {
        storageRef.listAll().addOnSuccessListener {
            for (item in it.items) {
                item.delete()
            }
        }
        onComplete()
    }


}
