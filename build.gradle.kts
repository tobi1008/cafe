// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Plugin cho ứng dụng Android, phiên bản 8.2.2 là một phiên bản ổn định gần đây
    id("com.android.application") version "8.2.2" apply false

    // ĐÃ NÂNG CẤP: Plugin cho ngôn ngữ Kotlin, nâng cấp lên 2.0.0 để tương thích
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false

    // Plugin cho các dịch vụ của Google (Firebase)
    id("com.google.gms.google-services") version "4.4.3" apply false

    // ĐÃ NÂNG CẤP: Plugin KSP, nâng cấp để tương thích với Kotlin 2.0.0
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
}

