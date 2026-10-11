package com.example.suhuhp

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // ===== View di layout =====
    private lateinit var teksJam: TextView
    private lateinit var teksMenit: TextView
    private lateinit var teksTanggal: TextView
    private lateinit var teksSuhuHp: TextView
    private lateinit var teksSuhuCpu: TextView
    private lateinit var teksBaterai: TextView
    private lateinit var teksStatus: TextView

    // ===== Handler untuk update berkala =====
    private val penangan = Handler(Looper.getMainLooper())

    // ===== Launcher pilih gambar dari storage =====
    private val pilihGambarLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            simpanGambarWidget(uri)
        } else {
            Toast.makeText(this, "Tidak ada gambar dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    // ===== Task berkala update suhu (tiap 2 detik) =====
    private val tugasBerkala = object : Runnable {
        override fun run() {
            perbaruiSemua()
            penangan.postDelayed(this, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ===== Bind view =====
        teksJam = findViewById(R.id.teksJam)
        teksMenit = findViewById(R.id.teksMenit)
        teksTanggal = findViewById(R.id.teksTanggal)
        teksSuhuHp = findViewById(R.id.teksSuhuHp)
        teksSuhuCpu = findViewById(R.id.teksSuhuCpu)
        teksBaterai = findViewById(R.id.teksBaterai)
        teksStatus = findViewById(R.id.teksStatus)

        // ===== Tombol Segarkan Suhu =====
        findViewById<Button>(R.id.tombolSegarkan).setOnClickListener {
            perbaruiSemua()
            segarkanWidget()
            Toast.makeText(this, "🔄 Suhu disegarkan", Toast.LENGTH_SHORT).show()
        }

        // ===== Tombol Pilih Model Widget =====
val prefs = PrefsHelper(this)

fun gantiModel(model: String) {
    prefs.modelWidget = model
    Toast.makeText(this, "✅ Model: ${model.uppercase()}", Toast.LENGTH_SHORT).show()
    segarkanWidget()
}

findViewById<Button>(R.id.btnModelKotak).setOnClickListener { gantiModel("kotak") }
findViewById<Button>(R.id.btnModelBulat).setOnClickListener { gantiModel("bulat") }
findViewById<Button>(R.id.btnModelMinimal).setOnClickListener { gantiModel("minimal") }
        // ===== Tombol Pilih Gambar Widget =====
        findViewById<Button>(R.id.tombolPilihGambar).setOnClickListener {
            pilihGambarLauncher.launch("image/*")
        }

        // ===== Tombol Reset ke Default =====
        findViewById<Button>(R.id.tombolResetGambar).setOnClickListener {
            val fileGambar = File(filesDir, "widget_bg.png")
            if (fileGambar.exists()) {
                fileGambar.delete()
                Toast.makeText(this, "✅ Gambar direset ke default", Toast.LENGTH_SHORT).show()
                segarkanWidget()
            } else {
                Toast.makeText(this, "Belum ada gambar custom", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ============================================================
    // SIMPAN GAMBAR DARI STORAGE KE INTERNAL APP
    // ============================================================
    private fun simpanGambarWidget(uri: Uri) {
        try {
            val fileGambar = File(filesDir, "widget_bg.png")

            // Baca gambar dari URI
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                Toast.makeText(this, "❌ Gagal baca gambar", Toast.LENGTH_SHORT).show()
                return
            }

            // Resize max 1200px biar tidak boros memori
            val maxSize = 1200
            val skala = if (bitmap.width > maxSize || bitmap.height > maxSize) {
                val rasio = bitmap.width.toFloat() / bitmap.height.toFloat()
                if (rasio > 1) {
                    maxSize.toFloat() / bitmap.width
                } else {
                    maxSize.toFloat() / bitmap.height
                }
            } else {
                1f
            }

            val bitmapResize = if (skala < 1f) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * skala).toInt(),
                    (bitmap.height * skala).toInt(),
                    true
                )
            } else {
                bitmap
            }

            // Simpan ke internal storage sebagai PNG
            fileGambar.outputStream().use { output ->
                bitmapResize.compress(Bitmap.CompressFormat.PNG, 100, output)
            }

            // Bersihkan memori
            if (bitmapResize != bitmap) bitmapResize.recycle()
            bitmap.recycle()

            Toast.makeText(this, "✅ Gambar tersimpan! Widget akan update.", Toast.LENGTH_LONG).show()

            // Refresh widget langsung
            segarkanWidget()

        } catch (e: Exception) {
            Toast.makeText(this, "❌ Gagal simpan: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ============================================================
    // PERBARUI SEMUA TAMPILAN DI APP
    // ============================================================
    private fun perbaruiSemua() {
        val kalender = Calendar.getInstance()
        val localeIndo = Locale("in", "ID")
        val formatHari = SimpleDateFormat("EEE", localeIndo)
        val formatTanggal = SimpleDateFormat("dd/MM", Locale.getDefault())

        val hari = formatHari.format(kalender.time).uppercase()
        val tanggal = formatTanggal.format(kalender.time)

        // Jam & tanggal
        teksJam.text = SimpleDateFormat("HH", Locale.getDefault()).format(kalender.time)
        teksMenit.text = SimpleDateFormat("mm", Locale.getDefault()).format(kalender.time)
        teksTanggal.text = "$hari\n$tanggal"

        // Suhu HP
        val suhuHp = PembacaSuhu.ambilSuhuBaterai(this)
        teksSuhuHp.text = "%.1f°C".format(suhuHp)

        // Suhu CPU
        val suhuCpu = PembacaSuhu.ambilSuhuCpu()
        teksSuhuCpu.text = if (suhuCpu > 0) "%.1f".format(suhuCpu) else "N/A"

        // Baterai
        teksBaterai.text = "⚡ ${PembacaSuhu.ambilLevelBaterai(this)}%"

        // Status suhu (warna)
        val status = PembacaSuhu.ambilStatus(suhuHp)
        teksStatus.text = status
        teksStatus.setTextColor(
            when {
                suhuHp < 40 -> 0xFF4CAF50.toInt()  // Hijau
                suhuHp < 45 -> 0xFFFF9800.toInt()  // Oranye
                else -> 0xFFF44336.toInt()          // Merah
            }
        )

        // Sinkronkan semua widget
        segarkanWidget()
    }

    // ============================================================
    // SEGARKAN SEMUA WIDGET (KOTAK + BULAT)
    // ============================================================
    private fun segarkanWidget() {
        val manajer = AppWidgetManager.getInstance(this)

        // ===== Widget Kotak =====
        try {
            val daftarIdKotak = manajer.getAppWidgetIds(
                ComponentName(this, WidgetSuhuProvider::class.java)
            )
            for (id in daftarIdKotak) {
                WidgetSuhuProvider.perbaruiWidget(this, manajer, id)
            }
        } catch (e: Exception) {
            // Widget kotak belum ditambahkan — abaikan
        }

        // ===== Widget Bulat =====
        try {
            val daftarIdBulat = manajer.getAppWidgetIds(
                ComponentName(this, WidgetSuhuBulatProvider::class.java)
            )
            for (id in daftarIdBulat) {
                WidgetSuhuBulatProvider.perbaruiWidget(this, manajer, id)
            }
        } catch (e: Exception) {
            // Widget bulat belum ditambahkan — abaikan
        }
    }

    // ============================================================
    // LIFECYCLE
    // ============================================================
    override fun onResume() {
        super.onResume()
        penangan.post(tugasBerkala)
    }

    override fun onPause() {
        super.onPause()
        penangan.removeCallbacks(tugasBerkala)
    }
}
