
package com.analyzer.virtuelparia

data class Prediction(
    val v1: Double,
    val draw: Double,
    val v2: Double,
    val recommendation: String
)

object PredictionEngine {

    fun analyze(
        v1Odds: Double,
        drawOdds: Double,
        v2Odds: Double
    ): Prediction {

        require(v1Odds > 1.0)
        require(drawOdds > 1.0)
        require(v2Odds > 1.0)

        val p1 = 1.0 / v1Odds
        val px = 1.0 / drawOdds
        val p2 = 1.0 / v2Odds

        val total = p1 + px + p2

        val v1 = p1 / total * 100
        val draw = px / total * 100
        val v2 = p2 / total * 100

        val recommendation = when {
            v1 >= draw && v1 >= v2 -> "V1"
            draw >= v1 && draw >= v2 -> "X"
            else -> "V2"
        }

        return Prediction(
            v1 = v1,
            draw = draw,
            v2 = v2,
            recommendation = recommendation
        )
    }
}
