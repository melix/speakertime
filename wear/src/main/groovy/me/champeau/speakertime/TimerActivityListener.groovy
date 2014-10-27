package me.champeau.speakertime

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import groovy.transform.CompileStatic
import me.champeau.speakertime.support.MessageConstants

@CompileStatic
class TimerActivityListener extends WearableListenerService {

    private int NOTIFICATION_ID = 0
    private int lastElapsedVibrate = -1
    private @Lazy Bitmap cachedBitmap = BitmapFactory.decodeResource(resources, R.drawable.speaker)

    @Override
    void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.path
        if (MessageConstants.MSG_TIME_LEFT==path) {
            updateNotification(messageEvent)
        } else if (MessageConstants.STOP_WEAR_ACTIVITY==path) {
            cancelNotification()
        }
    }

    private void cancelNotification() {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this)

        notificationManager.cancel(NOTIFICATION_ID)
        lastElapsedVibrate = -1
    }

    private void updateNotification(MessageEvent messageEvent) {
        String[] data = new String(messageEvent.data).split(';')
        String timeLeft = data[0]
        double elapsed = Double.valueOf(data[1])
        int rounded = (int) elapsed
        if (lastElapsedVibrate==-1 || timeLeft.endsWith(':0')) {
            Intent viewIntent = new Intent(this, WearPresentationActivity)

            PendingIntent viewPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            viewIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT)

            def bigStyle = new NotificationCompat.BigTextStyle()
            bigStyle.bigText """Time left for your presentation: $timeLeft
Elapsed time: ${rounded}%)
"""

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this)
            notificationBuilder.with {
                smallIcon = R.drawable.ic_action_alarms
                largeIcon = cachedBitmap
                contentTitle = 'Time left'
                contentText = "$timeLeft (Elapsed: ${rounded}%)"
                contentIntent = viewPendingIntent
                ongoing = true
                style = bigStyle
            }

            if (((int) (rounded / 10)) != lastElapsedVibrate) {
                lastElapsedVibrate = (int) (rounded / 10)
                long[] pattern = new long[lastElapsedVibrate + 1]
                for (int i = 0; i < pattern.length; i++) {
                    pattern[i] = 100 * (i + 1)
                }
                notificationBuilder.vibrate = pattern
            }

            // Get an instance of the NotificationManager service
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this)

            // Build the notification and issues it with notification manager.
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }
}