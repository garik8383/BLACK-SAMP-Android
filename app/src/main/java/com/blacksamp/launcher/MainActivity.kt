package com.blacksamp.launcher

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var status: TextView
    // Set this to the HTTPS URL of your own update manifest once BLACK SAMP Updates is ready.
    private val manifestUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 12, 14)
        window.navigationBarColor = Color.rgb(12, 12, 14)
        val gold = Color.rgb(234, 186, 73)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(32.dp, 16.dp, 32.dp, 16.dp)
            setBackgroundColor(Color.rgb(12, 12, 14))
        }
        fun label(value: String, size: Float, color: Int, bold: Boolean = false): TextView = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }
        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        left.addView(label("BLACK SAMP", 36f, gold, true))
        left.addView(label("Версия лаунчера 0.2.0", 13f, Color.LTGRAY))
        left.addView(View(this), LinearLayout.LayoutParams(1, 24.dp))
        status = label("Игровой Android-клиент пока не подключён.", 15f, Color.LTGRAY)
        left.addView(status)
        root.addView(left, LinearLayout.LayoutParams(0, -1, 1f))
        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20.dp, 0, 0, 0)
        }
        fun button(title: String, enabled: Boolean, onClick: () -> Unit) {
            val b = Button(this).apply {
                text = title
                isAllCaps = false
                isEnabled = enabled
                setTextColor(if (enabled) Color.BLACK else Color.LTGRAY)
                background = GradientDrawable().apply {
                    cornerRadius = 12.dp.toFloat()
                    setColor(if (enabled) gold else Color.rgb(52, 52, 55))
                }
                setOnClickListener { onClick() }
            }
            right.addView(b, LinearLayout.LayoutParams(-1, 54.dp).apply { topMargin = 10.dp })
        }
        button("Проверить обновления", true) { checkUpdates() }
        button("Установить игру — скоро", false) { }
        button("Играть — скоро", false) { }
        root.addView(right, LinearLayout.LayoutParams(0, -1, 1f))
        setContentView(root)
    }

    private fun checkUpdates() {
        if (manifestUrl.isBlank()) {
            status.text = "Сначала укажи адрес manifest.json в MainActivity.kt"
            return
        }
        status.text = "Проверяем обновления…"
        thread {
            val message = try {
                val conn = (URL(manifestUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000; readTimeout = 8000; instanceFollowRedirects = true
                }
                try {
                    if (conn.responseCode != 200) error("HTTP ${conn.responseCode}")
                    val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                    "Последняя версия: ${json.optString("version", "не указана")}. Загрузка файлов будет добавлена после подключения игрового клиента."
                } finally { conn.disconnect() }
            } catch (e: Exception) { "Ошибка проверки: ${e.message}" }
            runOnUiThread { status.text = message }
        }
    }
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
