
package com.analyzer.virtuelparia

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ImageAnalyzer(context: Context) {

    private val recognizer = TextRecognition
        .getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun analyze(
        bitmap: Bitmap,
        onResult: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { result ->
                onResult(result.text)
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    fun close() {
        recognizer.close()
    }
}
