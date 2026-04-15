package com.kelompok.jadwalku

import android.app.Application
import java.util.TimeZone

class PengingatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Makassar"))
    }
}