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
import android.widget.FrameLayout

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

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        count = prefs.getInt("total_chests", 0).coerceAtLeast(0)
        buildScreen()
        refresh()
    }

    private fun rounded(color: Int, radius: Float = 22f): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = dp(radius.toInt()).toFloat() }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = true
        }

    private fun button(text: String, color: Int, textColor: Int = Color.WHITE, action: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            setTextColor(textColor)
            textSize = 15f
            isAllCaps = false
            isSingleLine = false
            minHeight = 0
            minimumHeight = 0
            background = rounded(color, 18f)
            setPadding(dp(6), dp(6), dp(6), dp(6))
            setOnClickListener { action() }
        }

    private fun buildScreen() {
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setBackgroundColor(bg)
            isVerticalScrollBarEnabled = false
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(22), dp(18), dp(24))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(bg, Color.rgb(18, 25, 58))
            )
        }
        scroll.addView(root, FrameLayout.LayoutParams(-1, -2))
        setContentView(scroll)

        root.addView(label("CHEST TRACKER", 27f, white, true), LinearLayout.LayoutParams(-1, dp(48)))
        root.addView(label("FORTNITE CHEST COUNTER", 12f, cyan, true), LinearLayout.LayoutParams(-1, dp(26)))

        val countPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(panel, 26f)
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        root.addView(countPanel, LinearLayout.LayoutParams(-1, dp(176)).apply {
            topMargin = dp(20)
            bottomMargin = dp(18)
        })
        countPanel.addView(label("TOTAL CHESTS OPENED", 13f, muted, true))
        totalView = label("0", 58f, cyan, true)
        countPanel.addView(totalView, LinearLayout.LayoutParams(-1, dp(88)))
        countPanel.addView(label("Saved on this device", 13f, muted))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            weightSum = 2f
        }
        root.addView(row, LinearLayout.LayoutParams(-1, dp(62)))
        row.addView(
            button("＋ Add 1", Color.rgb(0, 132, 190)) { updateCount(count + 1) },
            LinearLayout.LayoutParams(0, -1, 1f).apply { rightMargin = dp(5) }
        )
        row.addView(
            button("－ Remove 1", Color.rgb(55, 69, 94)) { updateCount((count - 1).coerceAtLeast(0)) },
            LinearLayout.LayoutParams(0, -1, 1f).apply { leftMargin = dp(5) }
        )

        root.addView(
            button("＋10 CHESTS", Color.rgb(36, 53, 83), white) { updateCount(count + 10) },
            LinearLayout.LayoutParams(-1, dp(56)).apply {
                topMargin = dp(10)
                bottomMargin = dp(20)
            }
        )

        root.addView(label("SET A CUSTOM TOTAL", 13f, muted, true), LinearLayout.LayoutParams(-1, dp(28)))
        entry = EditText(this).apply {
            hint = "Enter chest count"
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 18f
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(29, 43, 70), 16f)
            setPadding(dp(14), dp(8), dp(14), dp(8))
            imeOptions = EditorInfo.IME_ACTION_DONE
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) { applyCustom(); true } else false
            }
        }
        root.addView(entry, LinearLayout.LayoutParams(-1, dp(54)).apply { topMargin = dp(8) })
        root.addView(
            button("Save custom total", Color.rgb(25, 154, 112)) { applyCustom() },
            LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(10) }
        )

        message = label("Your count is saved automatically.", 12f, muted)
        root.addView(message, LinearLayout.LayoutParams(-1, dp(44)))
        root.addView(
            button("Reset to 0", Color.rgb(68, 42, 59), Color.rgb(255, 190, 205)) {
                updateCount(0)
                message.text = "Counter reset to zero."
            },
            LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) }
        )
    }

    private fun applyCustom() {
        val value = entry.text.toString().toLongOrNull()
        if (value == null || value < 0 || value > Int.MAX_VALUE) {
            message.text = "Enter a valid whole number."
            return
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
