
package com.analyzer.virtuelparia

import android.content.Context

class AIAnalysis(context: Context) {

    private val history = HistoryManager(context)

    fun analyzeText(text: String): String {

        val odds = OddsExtractor.extract(text)

        val v1 = odds.v1
        val draw = odds.draw
        val v2 = odds.v2

        if (v1 == null || draw == null || v2 == null) {
            return """
                ANALYSE INCOMPLETE

                V1 = ${v1 ?: "--"}
                X = ${draw ?: "--"}
                V2 = ${v2 ?: "--"}

                Vérifiez les cotes sur la capture.
            """.trimIndent()
        }

        return try {
            val prediction = PredictionEngine.analyze(
                v1Odds = v1,
                drawOdds = draw,
                v2Odds = v2
            )

            history.save(
                prediction.v1,
                prediction.draw,
                prediction.v2,
                prediction.recommendation
            )

            """
                RÉSULTAT D'ANALYSE

                V1 = ${"%.1f".format(prediction.v1)} %
                X = ${"%.1f".format(prediction.draw)} %
                V2 = ${"%.1f".format(prediction.v2)} %

                TENDANCE : ${prediction.recommendation}

                Analyse basée sur les cotes.
                Résultat non garanti.
            """.trimIndent()

        } catch (e: IllegalArgumentException) {
            "COTES INVALIDES : vérifiez les valeurs."
        }
    }
}
