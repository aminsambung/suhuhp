package com.example.suhuhp

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var teksJam: TextView
    private lateinit var teksMenit: TextView
    private lateinit var teksTanggal: TextView
    private lateinit var teksSuhuHp: TextView
    private lateinit var teksSuhuCpu: TextView
    private lateinit var teksBaterai: TextView
    private lateinit var teksStatus: TextView

    private val penangan = Handler(Looper.getMainLooper())

    // Tugas berulang untuk update tampilan tiap 2 detik
    private val tugasBerkala = object : Runnable {
        override fun run() {
            perbaruiSemua()
            penangan.postDelayed(this, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        teksJam = findViewById(R.id.teksJam)
        teksMenit = findViewById(R.id.teksMenit)
        teksTanggal = findViewById(R.id.teksTanggal)
        teksSuhuHp = findViewById(R.id.teksSuhuHp)
        teksSuhuCpu = findViewById(R.id.teksSuhuCpu)
        teksBaterai = findViewById(R.id.teksBaterai)
        teksStatus = findViewById(R.id.teksStatus)

        findViewById<Button>(R.id.tombolSegarkan).setOnClickListener {
            perbaruiSemua()
            segarkanWidget()
        }
    }

    /** Memperbarui semua tampilan */
    private fun perbaruiSemua() {
        // ===== Jam & Tanggal =====
        val kalender = Calendar.getInstance()

        // Nama hari dalam Bahasa Indonesia
        val localeIndo = Locale("in", "ID")
        val formatHari = SimpleDateFormat("EEE", localeIndo)
        val formatTanggal = SimpleDateFormat("dd/MM", Locale.getDefault())

        val hari = formatHari.format(kalender.time).uppercase()
        val tanggal = formatTanggal.format(kalender.time)

        teksJam.text = SimpleDateFormat("HH", Locale.getDefault()).format(kalender.time)
        teksMenit.text = SimpleDateFormat("mm", Locale.getDefault()).format(kalender.time)
        teksTanggal.text = "$hari\n$tanggal"

        // ===== Suhu Baterai =====
        val suhuHp = PembacaSuhu.ambilSuhuBaterai(this)
        teksSuhuHp.text = "%.1f°C".format(suhuHp)

        // ===== Suhu CPU =====
        val suhuCpu = PembacaSuhu.ambilSuhuCpu()
        teksSuhuCpu.text = if (suhuCpu > 0) "%.1f".format(suhuCpu) else "N/A"

        // ===== Baterai =====
        teksBaterai.text = "⚡ ${PembacaSuhu.ambilLevelBaterai(this)}%"

        // ===== Status =====
        val status = PembacaSuhu.ambilStatus(suhuHp)
        teksStatus.text = status
        teksStatus.setTextColor(
            when {
                suhuHp < 40 -> 0xFF4CAF50.toInt() // hijau
                suhuHp < 45 -> 0xFFFF9800.toInt() // oranye
                else -> 0xFFF44336.toInt()         // merah
            }
        )

        // Sinkronkan widget
        segarkanWidget()
    }

    /** Memperbarui widget di homescreen */
    private fun segarkanWidget() {
        val manajer = AppWidgetManager.getInstance(this)
        val daftarId = manajer.getAppWidgetIds(
            ComponentName(this, WidgetSuhuProvider::class.java)
        )
        for (id in daftarId) {
            WidgetSuhuProvider.perbaruiWidget(this, manajer, id)
        }
    }

    override fun onResume() {
        super.onResume()
        penangan.post(tugasBerkala)
    }

    override fun onPause() {
        super.onPause()
        penangan.removeCallbacks(tugasBerkala)
    }
}
