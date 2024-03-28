package com.example.readr.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.readr.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Date

class FirebaseHandler {
    val db = Firebase.firestore
    val prefs = db.collection("preferences")

    var overlayTextSize = 20L

    fun loadTextSizes(onComplete:(Int)->Unit={}) {
        prefs.document("textSizes").get().addOnSuccessListener {
            overlayTextSize = it.get("overlayTextSize") as Long
        }
            .addOnFailureListener {
                Toast.makeText(MainActivity.context, "ERROR LOADING DATA. PLEASE CHECK YOUR INTERNET CONNECTION!", Toast.LENGTH_SHORT).show()
            }
    }

    fun saveOverlayTextSize(overlayTextSize:Int) {
        prefs.document("textSizes").get().addOnSuccessListener {
            val new = HashMap<String, Any>(it.data!!)
            new["overlayTextSize"] = overlayTextSize
            prefs.document("textSizes").set(new)
                .addOnSuccessListener { Log.w("fb_document", "SUCCESSFULLY SAVED OVERLAY TEXT SIZE") }
                .addOnFailureListener { e -> Log.w("fb_document", ":( FAILED TO SAVE OVERLAY TEXT SIZE", e) }
        }
    }


    fun sync() {
        // TODO: SYNC ALL SQLITE STUFF WITH FIREBASE BY LIKE DELETING ALL AND REMAKING IG
        // first, sync users
    }


    fun setDocument(collection:String, name:String, hm: HashMap<String, Any>) { // note that this may not fully finish before returning... but usually not a necessity because as long as other work then yes, this is for syncing
        db.collection(collection).document(name).set(hm)
            .addOnSuccessListener { Log.w("fb_document", "SUCCESSFULLY SAVED DOCUMENT") }
            .addOnFailureListener { e -> Log.w("fb_document", ":( FAILED TO SAVE DOCUMENT", e) }
    }


}