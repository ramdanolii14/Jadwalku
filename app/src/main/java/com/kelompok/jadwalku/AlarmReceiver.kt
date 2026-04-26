package com.kelompok.jadwalku

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * BroadcastReceiver yang menerima alarm dari AlarmManager.
 * Menampilkan notifikasi lokal — tidak membutuhkan koneksi internet sama sekali.
 *
 * Juga di-trigger saat device reboot via BootReceiver agar alarm yang sudah
 * terdaftar sebelum reboot bisa dijadwalkan ulang.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID    = "jadwalku_channel"
        const val EXTRA_TITLE   = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_DOC_ID  = "extra_doc_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title   = intent.getStringExtra(EXTRA_TITLE)   ?: "Pengingat Jadwalku"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""

        createNotificationChannel(context)

        // Android 13+ butuh POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Gunakan waktu sekarang sebagai notificationId agar tidak tumpang tindih
        val notifId = System.currentTimeMillis().toInt()
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pengingat Jadwal & Tugas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat jadwal dan deadline tugas"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}