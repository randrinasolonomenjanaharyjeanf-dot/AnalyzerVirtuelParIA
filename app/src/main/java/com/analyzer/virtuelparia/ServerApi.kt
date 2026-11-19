
package com.analyzer.virtuelparia

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object ServerApi {

    private const val SERVER_URL =
        "https://analyzervirtuelparia.onrender.com/analyze"

    fun analyzeImage(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            var connection: HttpURLConnection? = null

            try {
                val boundary = "Boundary-${UUID.randomUUID()}"
                connection = URL(SERVER_URL).openConnection()
                    as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 30000
                connection.readTimeout = 120000
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=$boundary"
                )

                val output = connection.outputStream
                val writer = output.bufferedWriter()

                writer.write("--$boundary\r\n")
                writer.write(
                    "Content-Disposition: form-data; name=\"image\"; filename=\"capture.jpg\"\r\n"
                )

                val mime = context.contentResolver.getType(imageUri)
                    ?: "image/jpeg"

                writer.write("Content-Type: $mime\r\n\r\n")
                writer.flush()

                context.contentResolver.openInputStream(imageUri)
                    ?.use { input ->
                        input.copyTo(output)
                    } ?: throw Exception("Tsy afaka namaky ilay sary")

                output.flush()
                writer.write("\r\n--$boundary--\r\n")
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                val responseStream =
                    if (responseCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                val response = responseStream
                    .bufferedReader()
                    .use { it.readText() }

                if (responseCode !in 200..299) {
                    throw Exception("Erreur serveur: $response")
                }

                val json = JSONObject(response)

                if (!json.optBoolean("success")) {
                    throw Exception("Tsy nahomby ny analyse")
                }

                val result = json.optJSONObject("result")
                    ?: throw Exception("Tsy nisy valiny AI")

                Handler(Looper.getMainLooper()).post {
                    onSuccess(result.toString())
                }

            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    onError(e.message ?: "Erreur de connexion")
                }
            } finally {
                connection?.disconnect()
            }
        }.start()
    }
}
