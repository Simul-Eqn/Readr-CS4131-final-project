package com.example.readr

import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.EntityAnnotation
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.protobuf.ByteString
import java.io.FileInputStream
import java.io.IOException

object DetectText {

    // Detects text in the specified image.
    @Throws(IOException::class)
    fun detectText(filePath: String, onError:(String)->Unit, onSuccess:(List<EntityAnnotation>)->Unit) {
        val imgBytes = ByteString.readFrom(FileInputStream(filePath))
        val img = Image.newBuilder().setContent(imgBytes).build()
        detectText(img, onError, onSuccess)
    }

    @Throws(IOException::class)
    fun detectText(img: Image, onError:(String)->Unit, onSuccess:(List<EntityAnnotation>)->Unit) {
        val requests: MutableList<AnnotateImageRequest> = ArrayList()
        val feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build()
        val request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build()
        requests.add(request)

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.

        ImageAnnotatorClient.create().use { client ->
            val response = client.batchAnnotateImages(requests)
            val responses = response.responsesList
            for (res in responses) {
                if (res.hasError()) {
                    //System.out.format("Error: %s%n", res.error.message)
                    onError(res.error.message)
                    return
                }

                onSuccess(res.textAnnotationsList)

                /*// For full list of available annotations, see http://g.co/cloud/vision/docs
                for (annotation in res.textAnnotationsList) {
                    System.out.format("Text: %s%n", annotation.description)
                    System.out.format("Position : %s%n", annotation.boundingPoly)
                }*/
            }
        }
    }
}