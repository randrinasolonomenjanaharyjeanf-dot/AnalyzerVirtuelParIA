
package com.analyzer.virtuelparia

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class HistoryManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        "analysis_history",
        Context.MODE_PRIVATE
    )

    fun save(
        v1: Double,
        draw: Double,
        v2: Double,
        prediction: String
    ) {
        val history = JSONArray(
            prefs.getString("records", "[]")
        )

        val item = JSONObject().apply {
            put("v1", v1)
            put("draw", draw)
            put("v2", v2)
            put("prediction", prediction)
            put("date", System.currentTimeMillis())
        }

        history.put(item)

        prefs.edit()
            .putString("records", history.toString())
            .apply()
    }

    fun getHistory(): JSONArray {
        return JSONArray(
            prefs.getString("records", "[]")
        )
    }

    fun clear() {
        prefs.edit()
            .remove("records")
            .apply()
    }
}
