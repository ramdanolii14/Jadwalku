package com.kelompok.jadwalku

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Menjadwalkan ulang semua alarm setelah device reboot.
 * AlarmManager kehilangan semua alarm ketika device dimatikan, sehingga
 * receiver ini diperlukan agar notifikasi tetap berfungsi.
 *
 * Setiap item mendapat 4 alarm: 2 jam, 1 jam, 5 menit, 3 menit sebelumnya.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return

        rescheduleJadwal(context)
        rescheduleTugas(context)
    }

    private fun rescheduleJadwal(context: Context) {
        FirestoreHelper.getJadwal { list ->
            list.forEach { item ->
                val eventMs = parseDateTime(item.tanggal, item.waktuMulai)
                val offsets = listOf(
                    2 * 60 * 60 * 1000L to "2 jam",
                    60 * 60 * 1000L     to "1 jam",
                    5 * 60 * 1000L      to "5 menit",
                    3 * 60 * 1000L      to "3 menit"
                )
                offsets.forEachIndexed { index, (offset, label) ->
                    val triggerAt = eventMs - offset
                    if (triggerAt > System.currentTimeMillis()) {
                        scheduleAlarm(
                            context   = context,
                            docId     = "${item.id}_j$index",
                            title     = "Jadwal Segera Dimulai",
                            message   = "${item.namaKegiatan} akan dimulai $label lagi.",
                            triggerAt = triggerAt
                        )
                    }
                }
            }
        }
    }

    private fun rescheduleTugas(context: Context) {
        FirestoreHelper.getTugas { list ->
            list.forEach { item ->
                val deadlineMs = parseDateTime(item.tanggalDeadline, item.waktuDeadline)
                val offsets = listOf(
                    2 * 60 * 60 * 1000L to "2 jam",
                    60 * 60 * 1000L     to "1 jam",
                    5 * 60 * 1000L      to "5 menit",
                    3 * 60 * 1000L      to "3 menit"
                )
                offsets.forEachIndexed { index, (offset, label) ->
                    val triggerAt = deadlineMs - offset
                    if (triggerAt > System.currentTimeMillis()) {
                        scheduleAlarm(
                            context   = context,
                            docId     = "${item.id}_t$index",
                            title     = "Deadline Tugas Mendekat!",
                            message   = "Tugas '${item.judul}' deadline ${item.tanggalDeadline} pukul ${item.waktuDeadline} (sisa ~$label).",
                            triggerAt = triggerAt
                        )
                    }
                }
            }
        }
    }

    private fun scheduleAlarm(context: Context, docId: String, title: String, message: String, triggerAt: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TITLE,   title)
            putExtra(AlarmReceiver.EXTRA_MESSAGE, message)
            putExtra(AlarmReceiver.EXTRA_DOC_ID,  docId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

        val pending = PendingIntent.getBroadcast(context, docId.hashCode(), intent, flags)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun parseDateTime(tanggal: String, waktu: String): Long {
        return try {
            val sdf = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
            sdf.parse("$tanggal $waktu")?.time ?: 0L
        } catch (e: Exception) { 0L }
    }
}