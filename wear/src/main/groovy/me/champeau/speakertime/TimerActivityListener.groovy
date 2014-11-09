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
        switch (messageEvent.path) {
            case MessageConstants.MSG_TIME_LEFT:
                updateNotification(messageEvent)
                break
            case MessageConstants.STOP_WEAR_ACTIVITY:
                cancelNotification()
                break
        }
    }

    private void cancelNotification() {
        compatNotificationManager.cancel(NOTIFICATION_ID)
        lastElapsedVibrate = -1
    }

    private void updateNotification(MessageEvent messageEvent) {
        String[] data = new String(messageEvent.data).split(';')
        String timeLeft = data[0]
        double elapsed = Double.valueOf(data[1])
        int rounded = (int) elapsed
        if (lastElapsedVibrate==-1 || timeLeft.endsWith(':0')) {
            // Build the notification and issues it with notification manager.
            notify(NOTIFICATION_ID) {
                smallIcon = R.drawable.ic_action_alarms
                largeIcon = cachedBitmap
                contentTitle = 'Time left'
                contentText = "$timeLeft (Elapsed: ${rounded}%)"
                contentIntent = pendingActivityIntent(0, intent(WearPresentationActivity), PendingIntent.FLAG_UPDATE_CURRENT)
                ongoing = true
                style = bigTextStyle {
                    bigText """Time left for your presentation: $timeLeft
Elapsed time: ${rounded}%)
"""
                }

                int elapsedVibrate = (int) (rounded / 10)
                if (elapsedVibrate != lastElapsedVibrate) {
                    lastElapsedVibrate = elapsedVibrate
                    vibrate = (1..lastElapsedVibrate+1).collect {
                        [100*it, 100*it]
                    }.flatten() as long[]
                }
            }
        }
    }
}