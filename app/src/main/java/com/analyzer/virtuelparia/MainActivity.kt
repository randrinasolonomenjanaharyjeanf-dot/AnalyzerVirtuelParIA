
package com.analyzer.virtuelparia

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 30, 24, 30)
            setBackgroundColor(Color.rgb(12, 20, 35))
        }

        val title = TextView(this).apply {
            text = "🧠 ANALYZER VIRTUEL PAR IA"
            textSize = 23f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "VIRTUAL FOOTBALL ANALYSIS"
            textSize = 13f
            setTextColor(Color.CYAN)
            gravity = Gravity.CENTER
        }

        val server = TextView(this).apply {
            text = "● IA SERVER : EN ATTENTE"
            textSize = 16f
            setTextColor(Color.GREEN)
            gravity = Gravity.CENTER
        }

        val capture = Button(this).apply {
            text = "📷 Importer une capture d'écran"
        }

        val analyze = Button(this).apply {
            text = "ANALYSER AVEC IA"
        }

        val resultTitle = TextView(this).apply {
            text = "RÉSULTAT D'ANALYSE"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val result = TextView(this).apply {
            text = "V1 = -- %\n\nX = -- %\n\nV2 = -- %"
            textSize = 21f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(server)
        layout.addView(capture)
        layout.addView(analyze)
        layout.addView(resultTitle)
        layout.addView(result)

        setContentView(layout)
    }
}
