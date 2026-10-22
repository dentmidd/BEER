package com.example.bearsleep

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var minutesInput: EditText
    private lateinit var status: TextView

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("bear", Context.MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        root.addView(TextView(this).apply {
            text = "🐻"
            textSize = 90f
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "Через скільки хвилин спати?"
            textSize = 20f
            gravity = Gravity.CENTER
        })

        minutesInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(prefs.getInt("min", 20).toString())
            textSize = 40f
            gravity = Gravity.CENTER
        }
        root.addView(minutesInput, LinearLayout.LayoutParams(dp(200), LinearLayout.LayoutParams.WRAP_CONTENT))

        val startBtn = Button(this).apply {
            text = "СТАРТ і відкрити YouTube"
            textSize = 20f
        }
        root.addView(startBtn, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        val cancelBtn = Button(this).apply { text = "Скасувати таймер" }
        root.addView(cancelBtn, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        status = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, 0)
        }
        root.addView(status)

        setContentView(root)

        startBtn.setOnClickListener { start(prefs) }
        cancelBtn.setOnClickListener {
            startService(Intent(this, TimerService::class.java).setAction(TimerService.ACTION_CANCEL))
            status.text = "Таймер скасовано."
        }
    }

    private fun start(prefs: android.content.SharedPreferences) {
        val minutes = minutesInput.text.toString().toIntOrNull()
        if (minutes == null || minutes < 1 || minutes > 600) {
            Toast.makeText(this, "Введіть число хвилин від 1 до 600", Toast.LENGTH_LONG).show()
            return
        }
        prefs.edit().putInt("min", minutes).apply()

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this,
                "Увімкніть «Поверх інших вікон» для цього застосунку, потім натисніть СТАРТ ще раз",
                Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")))
            return
        }

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, AdminReceiver::class.java)
        if (!dpm.isAdminActive(admin)) {
            Toast.makeText(this,
                "Натисніть «Активувати», потім СТАРТ ще раз",
                Toast.LENGTH_LONG).show()
            startActivity(Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Потрібно, щоб гасити екран, коли ведмедик іде спати."))
            return
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        startForegroundService(Intent(this, TimerService::class.java)
            .putExtra(TimerService.EXTRA_MIN, minutes))
        status.text = "Таймер запущено: $minutes хв."

        val yt = packageManager.getLaunchIntentForPackage("com.google.android.youtube")
        if (yt != null) startActivity(yt) else moveTaskToBack(true)
    }
}
