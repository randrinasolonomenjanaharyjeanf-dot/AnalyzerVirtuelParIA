
package com.analyzer.virtuelparia

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.graphics.Typeface

class MainActivity : Activity() {

    private lateinit var imageAnalyzer: ImageAnalyzer
    private lateinit var aiAnalysis: AIAnalysis

    private var selectedBitmap: Bitmap? = null

    private lateinit var status: TextView
    private lateinit var result: TextView
    private lateinit var imageInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageAnalyzer = ImageAnalyzer(this)
        aiAnalysis = AIAnalysis(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 28)
            setBackgroundColor(Color.rgb(12, 20, 35))
        }

        val scroll = ScrollView(this)
        scroll.addView(root)

        val title = TextView(this).apply {
            text = "🧠 ANALYZER VIRTUEL PAR IA"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 15, 0, 8)
        }

        val subtitle = TextView(this).apply {
            text = "VIRTUAL FOOTBALL ANALYSIS"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.CYAN)
        }

        status = TextView(this).apply {
            text = "● IA ANALYZER : PRÊT"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.GREEN)
            setPadding(0, 25, 0, 20)
        }

        val captureButton = Button(this).apply {
            text = "📷 IMPORTER UNE CAPTURE D'ÉCRAN"
        }

        imageInfo = TextView(this).apply {
            text = "Aucune capture sélectionnée"
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 12)
        }

        val analyzeButton = Button(this).apply {
            text = "🔍 ANALYSER AVEC IA"
        }

        val historyButton = Button(this).apply {
            text = "📊 VOIR L'HISTORIQUE"
        }

        val resultTitle = TextView(this).apply {
            text = "RÉSULTAT D'ANALYSE"
            textSize = 21f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 30, 0, 15)
        }

        result = TextView(this).apply {
            text = """
                V1 = -- %

                X = -- %

                V2 = -- %
                
                EN ATTENTE D'ANALYSE
            """.trimIndent()

            textSize = 19f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(15, 20, 15, 20)
        }

        val notice = TextView(this).apply {
            text = "Les résultats sont des estimations et ne garantissent pas le résultat du match."
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(10, 25, 10, 20)
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(status)
        root.addView(captureButton)
        root.addView(imageInfo)
        root.addView(analyzeButton)
        root.addView(resultTitle)
        root.addView(result)
        root.addView(historyButton)
        root.addView(notice)

        setContentView(scroll)

        captureButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, 100)
        }

        analyzeButton.setOnClickListener {
            analyzeScreenshot()
        }

        historyButton.setOnClickListener {
            val history = HistoryManager(this).getHistory()
            result.text = if (history.length() == 0) {
                "AUCUN HISTORIQUE DISPONIBLE"
            } else {
                buildString {
                    append("HISTORIQUE DES ANALYSES\n\n")
                    for (i in history.length() - 1 downTo 0) {
                        val item = history.getJSONObject(i)
                        append("Analyse ${i + 1}\n")
                        append("V1 = ${item.optDouble("v1")} %\n")
                        append("X = ${item.optDouble("draw")} %\n")
                        append("V2 = ${item.optDouble("v2")} %\n")
                        append("Tendance : ${item.optString("prediction")}\n\n")
                    }
                }
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return

            try {
                val stream = contentResolver.openInputStream(uri)
                selectedBitmap = stream?.use {
                    BitmapFactory.decodeStream(it)
                }

                if (selectedBitmap != null) {
                    imageInfo.text = "✓ CAPTURE D'ÉCRAN IMPORTÉE"
                    status.text = "● IMAGE PRÊTE POUR ANALYSE"
                } else {
                    imageInfo.text = "Impossible de lire cette image."
                }

            } catch (e: Exception) {
                imageInfo.text = "Erreur lors de l'importation."
            }
        }
    }

    private fun analyzeScreenshot() {

        val bitmap = selectedBitmap

        if (bitmap == null) {
            Toast.makeText(
                this,
                "Veuillez importer une capture d'écran.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        status.text = "● ANALYSE EN COURS..."
        result.text = "Lecture de la capture d'écran..."

        imageAnalyzer.analyze(
            bitmap,
            onResult = { extractedText ->

                val analysis = aiAnalysis.analyzeText(extractedText)

                result.text = analysis
                status.text = "● ANALYSE TERMINÉE"

            },
            onError = {
                result.text = "Erreur pendant la lecture de l'image."
                status.text = "● ERREUR D'ANALYSE"
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        imageAnalyzer.close()
    }
}
