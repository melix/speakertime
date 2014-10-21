package me.champeau.speakertime

import android.app.PendingIntent
import android.content.Intent
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

        Intent viewIntent = new Intent(this, WearPresentationActivity)

        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        viewIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT)

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_action_alarms)
                        .setContentTitle('Time left')
                        .setContentText("$timeLeft (Elapsed: ${rounded}%)")
                        .setContentIntent(viewPendingIntent)
                        .setOngoing(true)

        if (((int)(rounded/10))!=lastElapsedVibrate) {
            lastElapsedVibrate = (int) (rounded/10)
            long[] pattern = new long[lastElapsedVibrate+1]
            for (int i=0;i<pattern.length;i++) {
                pattern[i] = 100*(i+1)
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