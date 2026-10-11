package com.example.suhuhp

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class WidgetSuhuBulatProvider : AppWidgetProvider() {

    override fun onUpdate(
        konteks: Context,
        manajerWidget: AppWidgetManager,
        daftarId: IntArray
    ) {
        for (id in daftarId) perbaruiWidget(konteks, manajerWidget, id)
    }

    override fun onReceive(konteks: Context, intent: Intent) {
        super.onReceive(konteks, intent)
        if (intent.action == "com.example.suhuhp.SEGARKAN_WIDGET_BULAT") {
            val manajer = AppWidgetManager.getInstance(konteks)
            val daftarId = manajer.getAppWidgetIds(
                ComponentName(konteks, WidgetSuhuBulatProvider::class.java)
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
            val tampilan = RemoteViews(konteks.packageName, R.layout.widget_suhu_bulat)

            // ===== Jam =====
            val kalender = Calendar.getInstance()
            val localeIndo = Locale("in", "ID")

            val formatHari = SimpleDateFormat("EEEE", localeIndo)   // Nama hari panjang
            val formatJam = SimpleDateFormat("HH", Locale.getDefault())
            val formatMenit = SimpleDateFormat("mm", Locale.getDefault())

            tampilan.setTextViewText(R.id.bJam, formatJam.format(kalender.time))
            tampilan.setTextViewText(R.id.bMenit, formatMenit.format(kalender.time))
            tampilan.setTextViewText(R.id.bHari, formatHari.format(kalender.time).uppercase())

            // ===== Suhu HP =====
            val suhuHp = PembacaSuhu.ambilSuhuBaterai(konteks)
            tampilan.setTextViewText(R.id.bSuhuHp, "🌡️ %.1f°C".format(suhuHp))

            // ===== Suhu CPU =====
            val suhuCpu = PembacaSuhu.ambilSuhuCpu()
            tampilan.setTextViewText(
                R.id.bSuhuCpu,
                if (suhuCpu > 0) "  🔥 %.1f°C".format(suhuCpu) else "  🔥 N/A"
            )

            // ===== Baterai =====
            val baterai = PembacaSuhu.ambilLevelBaterai(konteks)
            tampilan.setTextViewText(R.id.bBaterai, "🔋 ${baterai}%")

            manajer.updateAppWidget(idWidget, tampilan)
        }
    }
}
