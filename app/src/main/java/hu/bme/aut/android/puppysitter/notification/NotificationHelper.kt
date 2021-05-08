package hu.bme.aut.android.puppysitter.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.model.User

class NotificationHelper(val context: Context) {
    companion object{
        const val EMAIL_NOTIFICATION_ID = 101
        const val CHANNEL_ID = "MatchNotificationChannel"
    }

    fun showMatchNotification(m: User) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.setType("text/plain")
        emailIntent.setData(Uri.parse("mailto:${Uri.encode(m.email)}"))
        val emailPendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, emailIntent, 0)
        createNotificationChannel()
        val noti = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.arrow_down_thick)
            .setContentTitle("It's a MATCH!")
            .setContentText("You'vel matched with ${m.userName}")
            .setContentIntent(emailPendingIntent)
        with(NotificationManagerCompat.from(context)) {
            notify(m.uid.hashCode(), noti.build())
        }
    }
        private fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val serviceChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Match Notification Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                val manager = context.getSystemService(
                    NotificationManager::class.java
                )
                manager.createNotificationChannel(serviceChannel)
            }
        }
}