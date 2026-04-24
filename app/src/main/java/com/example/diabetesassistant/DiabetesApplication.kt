package com.example.diabetesassistant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt 全局入口，Application 子类。
 * 必须在 AndroidManifest.xml 的 android:name 中声明此类。
 */
@HiltAndroidApp
class DiabetesApplication : Application()
