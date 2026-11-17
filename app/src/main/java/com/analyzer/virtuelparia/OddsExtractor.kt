
package com.analyzer.virtuelparia

data class MatchOdds(
    val v1: Double?,
    val draw: Double?,
    val v2: Double?
)

object OddsExtractor {

    private fun findOdds(
        text: String,
        labels: List<String>
    ): Double? {

        for (label in labels) {
            val regex = Regex(
                """(?i)\b${Regex.escape(label)}\b\s*[:=]?\s*(\d+[.,]\d+)"""
            )

            val match = regex.find(text)

            if (match != null) {
                return match.groupValues[1]
                    .replace(",", ".")
                    .toDoubleOrNull()
            }
        }

        return null
    }

    fun extract(text: String): MatchOdds {

        return MatchOdds(
            v1 = findOdds(text, listOf("V1", "1")),
            draw = findOdds(text, listOf("X", "DRAW")),
            v2 = findOdds(text, listOf("V2", "2"))
        )
    }
}
