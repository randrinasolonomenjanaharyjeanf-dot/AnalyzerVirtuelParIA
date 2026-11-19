
package com.analyzer.virtuelparia

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import android.graphics.Typeface
import org.json.JSONObject

class MainActivity : Activity() {

    private var selectedUri: Uri? = null

    private lateinit var status: TextView
    private lateinit var result: TextView
    private lateinit var imageInfo: TextView
    private lateinit var analyzeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            text = "● IA SERVER : CONNECTÉ"
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

        analyzeButton = Button(this).apply {
            text = "🔍 ANALYSER AVEC IA"
        }

        val resultTitle = TextView(this).apply {
            text = "RÉSULTAT D'ANALYSE IA"
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
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(15, 20, 15, 20)
        }

        val notice = TextView(this).apply {
            text = "Les probabilités sont des estimations basées sur les informations visibles. Elles ne garantissent pas le résultat du match."
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
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            selectedUri = data?.data

            if (selectedUri != null) {
                imageInfo.text = "✓ CAPTURE D'ÉCRAN IMPORTÉE"
                status.text = "● IMAGE PRÊTE POUR L'IA"
            } else {
                imageInfo.text = "Impossible de sélectionner l'image."
            }
        }
    }

    private fun analyzeScreenshot() {

        val uri = selectedUri

        if (uri == null) {
            Toast.makeText(
                this,
                "Veuillez importer une capture d'écran.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        status.text = "● CONNEXION AU SERVEUR IA..."
        result.text = "Envoi de la capture et analyse en cours..."
        analyzeButton.isEnabled = false

        ServerApi.analyzeImage(
            context = this,
            imageUri = uri,

            onSuccess = { jsonResult ->

                try {
                    val data = JSONObject(jsonResult)

                    val team1 = data.optString(
                        "team_v1", "Non détectée"
                    )
                    val team2 = data.optString(
                        "team_v2", "Non détectée"
                    )
                    val tournament = data.optString(
                        "tournament", "Non détecté"
                    )

                    result.text = """
                        ⚽ MATCH ANALYSÉ

                        $team1  VS  $team2

                        TOURNOI :
                        $tournament

                        ━━━━━━━━━━━━━━━

                        COTES DÉTECTÉES

                        V1 : ${showValue(data, "odds_v1")}
                        X  : ${showValue(data, "odds_x")}
                        V2 : ${showValue(data, "odds_v2")}

                        ━━━━━━━━━━━━━━━

                        PROBABILITÉS ESTIMÉES

                        V1 : ${showValue(data, "probability_v1")} %
                        X  : ${showValue(data, "probability_x")} %
                        V2 : ${showValue(data, "probability_v2")} %

                        ━━━━━━━━━━━━━━━

                        ANALYSE IA :

                        ${data.optString("analysis", "Aucune analyse disponible")}

                        CONFIANCE :
                        ${data.optString("confidence", "Non précisée")}

                        ANALYSE COMPLÈTE :
                        ${if (data.optBoolean("complete")) "OUI" else "NON"}
                    """.trimIndent()

                    status.text = "● ANALYSE IA TERMINÉE"
                    status.setTextColor(Color.GREEN)

                } catch (e: Exception) {
                    result.text = "Erreur de lecture de la réponse IA : ${e.message}"
                    status.text = "● ERREUR"
                }

                analyzeButton.isEnabled = true
            },

            onError = { error ->

                result.text = """
                    IMPOSSIBLE D'ANALYSER

                    $error

                    Vérifiez votre connexion Internet
                    et réessayez.
                """.trimIndent()

                status.text = "● ERREUR SERVEUR"
                status.setTextColor(Color.RED)
                analyzeButton.isEnabled = true
            }
        )
    }

    private fun showValue(
        data: JSONObject,
        key: String
    ): String {
        val value = data.opt(key)

        return if (value == null ||
            value == JSONObject.NULL ||
            value.toString() == ""
        ) {
            "--"
        } else {
            value.toString()
        }
    }
}
