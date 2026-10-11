package com.example.suhuhp

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.io.File

object PembacaSuhu {

    /** Membaca suhu baterai dalam derajat Celsius */
    fun ambilSuhuBaterai(konteks: Context): Float {
        val intent = konteks.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val persepuluhan = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return persepuluhan / 10.0f
    }

    /** Membaca suhu CPU dari thermal zone */
    fun ambilSuhuCpu(): Float {
        val daftarPath = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone2/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp"
        )
        for (path in daftarPath) {
            try {
                val berkas = File(path)
                if (berkas.exists() && berkas.canRead()) {
                    val nilai = berkas.readText().trim().toFloat()
                    val suhu = if (nilai > 1000) nilai / 1000f else nilai
                    if (suhu in 10f..120f) return suhu
                }
            } catch (_: Exception) { }
        }
        return -1f // tidak terdeteksi
    }

    /** Mengambil persentase baterai */
    fun ambilLevelBaterai(konteks: Context): Int {
        val bm = konteks.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    /** Menentukan status suhu berdasarkan nilai */
    fun ambilStatus(suhu: Float): String = when {
        suhu < 0 -> "❓ Tidak Terdeteksi"
        suhu < 35 -> "❄️ Dingin"
        suhu < 40 -> "✅ Normal"
        suhu < 45 -> "⚠️ Hangat"
        suhu < 50 -> "🔥 Panas"
        else -> "🚨 SANGAT PANAS!"
    }
}
