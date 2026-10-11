import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// ============================================================
// BACA PROPERTIES KEYSTORE
// ============================================================
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        load(keystorePropsFile.inputStream())
    }
}

// ============================================================
// FUNGSI VERSIONING OTOMATIS DARI GIT TAG
// ============================================================

/**
 * Mengambil versi dari Git tag terakhir.
 * Contoh: tag "v1.2.3" → return "1.2.3"
 * Jika tidak ada tag → fallback "1.0.0"
 */
fun getVersionNameFromGit(): String {
    return try {
        val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
            .directory(rootProject.projectDir)
            .redirectErrorStream(true)
            .start()
        val tag = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        when {
            tag.isEmpty() -> "1.0.0"
            tag.startsWith("v") -> tag.substring(1)
            else -> tag
        }
    } catch (e: Exception) {
        "1.0.0"
    }
}

/**
 * Mengkonversi versi "1.2.3" ke versionCode integer.
 * Rumus: major * 10000 + minor * 100 + patch
 * Contoh: 1.2.3 → 10203
 */
fun getVersionCodeFromGit(): Int {
    return try {
        val parts = getVersionNameFromGit().split(".")
        val major = parts.getOrNull(0)?.toIntOrNull() ?: 1
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
        major * 10000 + minor * 100 + patch
    } catch (e: Exception) {
        1
    }
}

// Simpan nilai versi agar dipakai konsisten
val appVersionName: String = getVersionNameFromGit()
val appVersionCode: Int = getVersionCodeFromGit()

// ============================================================
// KONFIGURASI ANDROID
// ============================================================
android {
    namespace = "com.example.suhuhp"
    compileSdk = 34

    // ---------- Default Config ----------
    defaultConfig {
        applicationId = "com.example.suhuhp"
        minSdk = 24
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Vector drawable support
        vectorDrawables {
            useSupportLibrary = true
        }

        // Resource config — hanya bahasa Indonesia & English
        resourceConfigurations += listOf("in", "en")

        // BuildConfig custom fields
        buildConfigField("String", "APP_VERSION_NAME", "\"$appVersionName\"")
        buildConfigField("int", "APP_VERSION_CODE", "$appVersionCode")
        buildConfigField("String", "BUILD_TIME", "\"${System.currentTimeMillis()}\"")
    }

    // ---------- Signing Configs ----------
    signingConfigs {
        create("release") {
            if (keystorePropsFile.exists()) {
                storeFile = file(keystoreProps.getProperty("storeFile") ?: "release.keystore")
                storePassword = keystoreProps.getProperty("storePassword") ?: ""
                keyAlias = keystoreProps.getProperty("keyAlias") ?: ""
                keyPassword = keystoreProps.getProperty("keyPassword") ?: ""

                // Aktifkan V1 & V2 signing (kompatibel Android lama & baru)
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = false
            }
        }
    }

    // ---------- Build Types ----------
    buildTypes {
        // ===== DEBUG =====
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true

            buildConfigField("boolean", "IS_DEBUG", "true")
            buildConfigField("String", "BASE_URL", "\"https://dev.example.com/\"")
        }

        // ===== RELEASE =====
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Signing config hanya dipakai jika keystore tersedia
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }

            buildConfigField("boolean", "IS_DEBUG", "false")
            buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
        }
    }

    // ---------- Compile Options ----------
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // ---------- Kotlin Options ----------
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }

    // ---------- Build Features ----------
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // ---------- Packaging ----------
    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module"
            )
        }
    }

    // ---------- Bundle Config (untuk AAB) ----------
    bundle {
        language {
            enableSplit = false // Gabungkan semua bahasa jadi satu
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }

    // ---------- Lint ----------
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        warningsAsErrors = false
        disable += setOf(
            "MissingTranslation",
            "ExtraTranslation"
        )
    }

    // ---------- Test Options ----------
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
}

// ============================================================
// DEPENDENCIES
// ============================================================
dependencies {
    // ===== AndroidX Core =====
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // ===== Material Design =====
    implementation("com.google.android.material:material:1.12.0")

    // ===== Layout =====
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // ===== Lifecycle =====
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")

    // ===== Coroutines =====
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ===== Splash Screen (Android 12+) =====
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ===== Testing =====
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:runner:1.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
}

// ============================================================
// TASK KUSTOM — INFO BUILD
// ============================================================

/**
 * Task untuk menampilkan info versi saat build
 * Jalankan: ./gradlew infoVersi
 */
tasks.register("infoVersi") {
    group = "informasi"
    description = "Menampilkan informasi versi aplikasi"
    doLast {
        println("╔══════════════════════════════════════════╗")
        println("║       INFO VERSI APLIKASI SUHU HP        ║")
        println("╠══════════════════════════════════════════╣")
        println("║  Nama Versi  : $appVersionName")
        println("║  Kode Versi  : $appVersionCode")
        println("║  Namespace   : com.example.suhuhp")
        println("║  Min SDK     : 24 (Android 7.0)")
        println("║  Target SDK  : 34 (Android 14)")
        println("║  Build Type  : Release")
        println("╚══════════════════════════════════════════╝")
    }
}

/**
 * Task untuk cek kesiapan signing keystore
 * Jalankan: ./gradlew cekKeystore
 */
tasks.register("cekKeystore") {
    group = "verifikasi"
    description = "Memeriksa apakah keystore siap untuk release"
    doLast {
        println("╔══════════════════════════════════════════╗")
        println("║        CEK KESIAPAN KEYSTORE             ║")
        println("╠══════════════════════════════════════════╣")
        if (keystorePropsFile.exists()) {
            println("║  ✅ keystore.properties  : DITEMUKAN")
            println("║  ✅ storeFile            : ${keystoreProps.getProperty("storeFile")}")
            println("║  ✅ keyAlias             : ${keystoreProps.getProperty("keyAlias")}")
        } else {
            println("║  ❌ keystore.properties  : TIDAK ADA")
            println("║  ⚠️  Release APK tidak akan tersigned!")
            println("║  💡 Buat file keystore.properties di root project")
        }
        println("╚══════════════════════════════════════════╝")
    }
}
