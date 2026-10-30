package com.example.ywts

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

object Reminder {
    private const val CHANNEL = "reminder"
    private const val WORK = "reminder_work"

    fun createChannel(context: Context) {
        val ch = NotificationChannel(CHANNEL, "Сануулга", NotificationManager.IMPORTANCE_DEFAULT)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    /** Апп-аас гарсны дараа 24 цаг тутам. */
    fun schedule(context: Context) {
        val req = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK)
    }

    fun show(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Санхүүгийн бүртгэл")
            .setContentText("Өнөөдрийн орлого, зарлагаа бүртгэхээ мартав аа?")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(1, n)
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Reminder.show(applicationContext)
        return Result.success()
    }
}
