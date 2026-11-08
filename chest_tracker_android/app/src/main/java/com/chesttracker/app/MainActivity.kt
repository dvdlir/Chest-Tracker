package com.chesttracker.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.content.Context
import android.widget.*

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("chest_tracker_local", Context.MODE_PRIVATE) }
    private var count = 0
    private lateinit var totalView: TextView
    private lateinit var entry: EditText
    private lateinit var message: TextView

    private val bg = Color.rgb(10, 16, 32)
    private val panel = Color.rgb(20, 31, 55)
    private val cyan = Color.rgb(30, 190, 255)
    private val white = Color.rgb(245, 248, 255)
    private val muted = Color.rgb(160, 178, 205)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        count = prefs.getInt("total_chests", 0).coerceAtLeast(0)
        buildScreen()
        refresh()
    }

    private fun rounded(color: Int, radius: Float = 22f): GradientDrawable = GradientDrawable().apply {
        setColor(color); cornerRadius = radius
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false): TextView = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        gravity = Gravity.CENTER
    }

    private fun button(text: String, color: Int, textColor: Int = Color.WHITE, action: () -> Unit): Button = Button(this).apply {
        this.text = text; setTextColor(textColor); textSize = 16f
        isAllCaps = false
        background = rounded(color, 18f)
        setPadding(8, 8, 8, 8)
        setOnClickListener { action() }
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(22, 28, 22, 24)
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(bg, Color.rgb(18, 25, 58)))
        }
        setContentView(root)

        root.addView(label("CHEST TRACKER", 27f, white, true), LinearLayout.LayoutParams(-1, 52))
        root.addView(label("FORTNITE CHEST COUNTER", 12f, cyan, true), LinearLayout.LayoutParams(-1, 28))

        val countPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            background = rounded(panel, 26f); setPadding(18, 24, 18, 24)
        }
        val panelParams = LinearLayout.LayoutParams(-1, 190).apply { topMargin = 26; bottomMargin = 22 }
        root.addView(countPanel, panelParams)
        countPanel.addView(label("TOTAL CHESTS OPENED", 13f, muted, true))
        totalView = label("0", 58f, cyan, true)
        countPanel.addView(totalView, LinearLayout.LayoutParams(-1, 100))
        countPanel.addView(label("Saved on this device", 13f, muted))

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; weightSum = 2f }
        root.addView(row, LinearLayout.LayoutParams(-1, 64))
        row.addView(button("＋  Add 1", Color.rgb(0, 132, 190)) { updateCount(count + 1) }, LinearLayout.LayoutParams(0, -1, 1f).apply { rightMargin = 7 })
        row.addView(button("－  Remove 1", Color.rgb(55, 69, 94)) { updateCount((count - 1).coerceAtLeast(0)) }, LinearLayout.LayoutParams(0, -1, 1f).apply { leftMargin = 7 })
        root.addView(button("＋10 CHESTS", Color.rgb(36, 53, 83), white) { updateCount(count + 10) }, LinearLayout.LayoutParams(-1, 58).apply { topMargin = 12; bottomMargin = 26 })

        root.addView(label("SET A CUSTOM TOTAL", 13f, muted, true), LinearLayout.LayoutParams(-1, 28))
        entry = EditText(this).apply {
            hint = "Enter chest count"
            setHintTextColor(muted); setTextColor(white); textSize = 18f
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setSingleLine(true); gravity = Gravity.CENTER
            background = rounded(Color.rgb(29, 43, 70), 16f)
            setPadding(14, 8, 14, 8)
            imeOptions = EditorInfo.IME_ACTION_DONE
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) { applyCustom(); true } else false
            }
        }
        root.addView(entry, LinearLayout.LayoutParams(-1, 58).apply { topMargin = 8 })
        root.addView(button("Save custom total", Color.rgb(25, 154, 112)) { applyCustom() }, LinearLayout.LayoutParams(-1, 56).apply { topMargin = 10 })

        message = label("Your count is saved automatically.", 12f, muted)
        root.addView(message, LinearLayout.LayoutParams(-1, 48))
        root.addView(button("Reset to 0", Color.rgb(68, 42, 59), Color.rgb(255, 190, 205)) {
            updateCount(0); message.text = "Counter reset to zero."
        }, LinearLayout.LayoutParams(-1, 48).apply { topMargin = 10 })
    }

    private fun applyCustom() {
        val value = entry.text.toString().toLongOrNull()
        if (value == null || value < 0 || value > Int.MAX_VALUE) {
            message.text = "Enter a valid whole number."; return
        }
        updateCount(value.toInt())
        message.text = "Custom total saved on this device."
        entry.clearFocus()
    }

    private fun updateCount(value: Int) {
        count = value.coerceAtLeast(0)
        prefs.edit().putInt("total_chests", count).apply()
        refresh()
    }

    private fun refresh() {
        if (::totalView.isInitialized) totalView.text = String.format("%,d", count)
        if (::entry.isInitialized && !entry.hasFocus()) entry.setText(count.toString())
    }
}
