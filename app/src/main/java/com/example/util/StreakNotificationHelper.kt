package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.ui.theme.Localization

object StreakNotificationHelper {
    const val CHANNEL_ID = "kodemamas_daily_streaks"
    const val CHANNEL_NAME = "Daily Streak & Lesson Reminders"
    const val NOTIFICATION_ID_STREAK = 1001
    const val NOTIFICATION_ID_LESSON = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Keeps your daily coding streak active with interactive South African lessons"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun showStreakNotification(
        context: Context,
        streakDays: Int,
        xp: Int,
        langCode: String = "en"
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val title = when (langCode) {
            "zu" -> "🔥 Isikhathi Sokufunda se-KodeMamas: Usuku $streakDays!"
            "xh" -> "🔥 Ixesha Lokufunda le-KodeMamas: Usuku $streakDays!"
            "af" -> "🔥 KodeMamas Leertyd: Dag $streakDays Aaneenlopend!"
            "nso" -> "🔥 Nako ya go Ithuta ya KodeMamas: Letšatši $streakDays!"
            "tn" -> "🔥 Nako ya go Ithuta ya KodeMamas: Letsatsi $streakDays!"
            "st" -> "🔥 Nako ya ho Ithuta ya KodeMamas: Letsatsi $streakDays!"
            "ts" -> "🔥 Nkarhi wo Dondza wa KodeMamas: Siku $streakDays!"
            "ss" -> "🔥 Sikhatsi sekuFundza se-KodeMamas: Lilanga $streakDays!"
            "ve" -> "🔥 Tshifhinga tsha u Guda tsha KodeMamas: Duvha $streakDays!"
            "nr" -> "🔥 Isikhathi Sokufunda se-KodeMamas: Ilanga $streakDays!"
            "sasl" -> "🔥 [Visual Sign] KodeMamas Streak: Day $streakDays!"
            else -> "🔥 KodeMamas Daily Streak Active: Day $streakDays!"
        }

        val content = when (langCode) {
            "zu" -> "Gcina umlilo uvutha! Qedela isifundo sakho se-5 min namuhla ukuze ukhule emakhonweni obuchwepheshe."
            "xh" -> "Gcina umlilo uvutha! Gqibezela isifundo sakho se-5 min namhlanje ukuze ukhule kubuchwepheshe."
            "af" -> "Hou die vuur aan die brand! Voltooi jou 5-minute les vandag om jou tegnologie-toekoms te bou."
            "nso" -> "Hlohleletša bokamoso! Feditša thuto ya metsotso e 5 lehono go gola bokgoning bja theknoloji."
            "tn" -> "Kgothatsa bokamoso! Fetsa thuto ya metsotso e 5 gompieno go gola mo thekenolojing."
            "st" -> "Khothatsa bokamoso! Qetela thuto ya metsotso e 5 kajeno ho hola theknolojing."
            "ts" -> "Hlayisa matimba! Hetisa dyondzo ya timinete ta 5 namuntlha u vumba vumundzuku bya wena."
            "ss" -> "Gcina umdlandla! Cedza sifundvo semaminiti la-5 lamuhla kute wakhe likusasa lakho."
            "ve" -> "Ṱuṱuwedzani vhumatshelo! Fhedzani ngudo ya miminete ya 5 ṋamusi u vhumba vhutsila hanu."
            "nr" -> "Bulunga umdlandla! Qeda isifundo semizuzu emi-5 namhlanje ukuze wakhe ingomuso lakho."
            "sasl" -> "[Visual Sign] Keep your coding flame alive! Complete your 5-min visual coding lesson today."
            else -> "Keep the fire burning! Complete today's 5-minute interactive lesson to build your tech future."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val managerCompat = NotificationManagerCompat.from(context)
            if (managerCompat.areNotificationsEnabled()) {
                managerCompat.notify(NOTIFICATION_ID_STREAK, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted on Android 13+, safely ignored
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun showLessonCompletedNotification(
        context: Context,
        lessonTitle: String,
        streakDays: Int,
        langCode: String = "en"
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val title = "🎉 Halala! Lesson Completed: $lessonTitle"
        val content = "Awesome achievement! Your daily streak is now $streakDays Days Active 🔥 (+50 XP). Keep building!"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val managerCompat = NotificationManagerCompat.from(context)
            if (managerCompat.areNotificationsEnabled()) {
                managerCompat.notify(NOTIFICATION_ID_LESSON, builder.build())
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }
}
