package com.example.testapk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var infoLayout: LinearLayout
    private lateinit var statusText: TextView

    // Этот "приемник" будет ловить изменения батареи
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val percentage = (level * 100 / scale.toFloat()).toInt()

            val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
            val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

            val statusString = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Заряжается"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Разряжается"
                BatteryManager.BATTERY_STATUS_FULL -> "Заряжена"
                else -> "Неизвестно"
            }

            // Обновляем текст на экране
            statusText.text = """
                Уровень: $percentage%
                Напряжение: ${voltage} мВ
                Температура: ${temperature / 10.0}°C
                Статус: $statusString
            """.trimIndent()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаем программный интерфейс
        infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        statusText = TextView(this).apply {
            textSize = 22f
        }

        infoLayout.addView(statusText)
        setContentView(infoLayout)

        // Регистрируем приемник для получения данных о батарее
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onDestroy() {
        super.onDestroy()
        // Обязательно отписываемся, чтобы не было утечек
        unregisterReceiver(batteryReceiver)
    }
}
