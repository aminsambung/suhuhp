package com.example.suhuhp

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class WidgetSuhuProvider : AppWidgetProvider() {

    override fun onUpdate(
        konteks: Context,
        manajerWidget: AppWidgetManager,
        daftarId: IntArray
    ) {
        for (id in daftarId) perbaruiWidget(konteks, manajerWidget, id)
    }

    override fun onReceive(konteks: Context, intent: Intent) {
        super.onReceive(konteks, intent)
        if (intent.action == "com.example.suhuhp.SEGARKAN_WIDGET") {
            val manajer = AppWidgetManager.getInstance(konteks)
            val daftarId = manajer.getAppWidgetIds(
                ComponentName(konteks, WidgetSuhuProvider::class.java)
            )
            for (id in daftarId) perbaruiWidget(konteks, manajer, id)
        }
    }

    companion object {
        fun perbaruiWidget(
            konteks: Context,
            manajer: AppWidgetManager,
            idWidget: Int
        ) {
            // Baca model yang dipilih user
            val prefs = PrefsHelper(konteks)
            val model = prefs.modelWidget

            // Pilih layout berdasarkan model
            val layoutId = when (model) {
                "bulat" -> R.layout.widget_suhu_bulat
                "minimal" -> R.layout.widget_suhu_minimal
                "digital" -> R.layout.widget_suhu_digital
                else -> R.layout.widget_suhu  // default: kotak
            }

            val tampilan = RemoteViews(konteks.packageName, layoutId)

            // Ambil data
            val kalender = Calendar.getInstance()
            val localeIndo = Locale("in", "ID")

            val formatHariPendek = SimpleDateFormat("EEE", localeIndo)
            val formatHariPanjang = SimpleDateFormat("EEEE", localeIndo)
            val formatTanggal = SimpleDateFormat("dd/MM", Locale.getDefault())
            val formatJam = SimpleDateFormat("HH", Locale.getDefault())
            val formatMenit = SimpleDateFormat("mm", Locale.getDefault())

            val hariPendek = formatHariPendek.format(kalender.time).uppercase()
            val hariPanjang = formatHariPanjang.format(kalender.time).uppercase()
            val tanggal = formatTanggal.format(kalender.time)
            val jam = formatJam.format(kalender.time)
            val menit = formatMenit.format(kalender.time)

            val suhuHp = PembacaSuhu.ambilSuhuBaterai(konteks)
            val suhuCpu = PembacaSuhu.ambilSuhuCpu()
            val baterai = PembacaSuhu.ambilLevelBaterai(konteks)

            // Background custom (kalau ada)
            val fileGambar = File(konteks.filesDir, "widget_bg.png")

            // Isi data berdasarkan model
            when (model) {
                "bulat" -> {
                    // Model bulat
                    if (fileGambar.exists()) {
                        try {
                            val bitmap = BitmapFactory.decodeFile(fileGambar.absolutePath)
                            if (bitmap != null) {
                                tampilan.setImageViewBitmap(R.id.bgBulat, bitmap)
                            }
                        } catch (_: Exception) {}
                    }
                    tampilan.setTextViewText(R.id.bJam, jam)
                    tampilan.setTextViewText(R.id.bMenit, menit)
                    tampilan.setTextViewText(R.id.bHari, hariPanjang)
                    tampilan.setTextViewText(R.id.bSuhuHp, "🌡️ %.1f°C".format(suhuHp))
                    tampilan.setTextViewText(
                        R.id.bSuhuCpu,
                        if (suhuCpu > 0) "🔥 %.1f°C".format(suhuCpu) else "🔥 N/A"
                    )
                    tampilan.setTextViewText(R.id.bBaterai, "🔋 ${baterai}%")
                }

                "minimal" -> {
                    // Model minimal (contoh — nanti kamu tambah)
                    tampilan.setTextViewText(R.id.mJam, "$jam:$menit")
                    tampilan.setTextViewText(R.id.mSuhu, "%.1f°C".format(suhuHp))
                }

                "digital" -> {
                    // Model digital (contoh — nanti kamu tambah)
                    tampilan.setTextViewText(R.id.dJam, jam)
                    tampilan.setTextViewText(R.id.dMenit, menit)
                    tampilan.setTextViewText(R.id.dHari, hariPanjang)
                    tampilan.setTextViewText(R.id.dSuhu, "%.1f°C".format(suhuHp))
                }

                else -> {
                    // Model kotak (default)
                    if (fileGambar.exists()) {
                        try {
                            val bitmap = BitmapFactory.decodeFile(fileGambar.absolutePath)
                            if (bitmap != null) {
                                tampilan.setImageViewBitmap(R.id.bgGambarWidget, bitmap)
                            }
                        } catch (_: Exception) {}
                    }
                    tampilan.setTextViewText(R.id.wHari, hariPendek)
                    tampilan.setTextViewText(R.id.wTanggal, tanggal)
                    tampilan.setTextViewText(R.id.wJam, jam)
                    tampilan.setTextViewText(R.id.wMenit, menit)
                    tampilan.setTextViewText(R.id.wSuhuHp, "%.1f°C".format(suhuHp))
                    tampilan.setTextViewText(
                        R.id.wSuhuCpu,
                        if (suhuCpu > 0) "%.1f°C".format(suhuCpu) else "N/A"
                    )
                    tampilan.setTextViewText(R.id.wBaterai, "${baterai}%")
                }
            }

            manajer.updateAppWidget(idWidget, tampilan)
        }
    }
}
