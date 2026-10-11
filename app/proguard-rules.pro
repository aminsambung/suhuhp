# Simpan nama class widget (WAJIB untuk AppWidgetProvider)
-keep class com.example.suhuhp.WidgetSuhuProvider { *; }
-keep class com.example.suhuhp.PembacaSuhu { *; }

# Simpan RemoteViews layout references
-keep class **.R$layout { *; }
-keep class **.R$id { *; }
-keep class **.R$drawable { *; }

# Kotlin metadata
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# AndroidX
-keep class androidx.appcompat.** { *; }
