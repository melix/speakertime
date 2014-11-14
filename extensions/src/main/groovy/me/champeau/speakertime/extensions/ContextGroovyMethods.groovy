package me.champeau.speakertime.extensions

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import groovy.transform.CompileStatic

@CompileStatic
class ContextGroovyMethods {
    static NotificationManagerCompat getCompatNotificationManager(Context self) {
        NotificationManagerCompat.from(self)
    }

    static void notify(Context self, int notificationId, Notification notification) {
        getCompatNotificationManager(self).notify(notificationId, notification)
    }

    static Notification notification(Context self, @DelegatesTo(NotificationCompat.Builder) Closure notificationSpec) {
        def builder = new NotificationCompat.Builder(self)
        builder.with(notificationSpec)
        builder.build()
    }

    static void notify(Context self, int notificationId, @DelegatesTo(NotificationCompat.Builder) Closure notificationSpec) {
        notify(self, notificationId, notification(self, notificationSpec))
    }

    static PendingIntent pendingActivityIntent(Context self, int requestCode, Intent intent, int flags) {
        PendingIntent.getActivity(self, requestCode, intent, flags)
    }

    static NotificationCompat.BigTextStyle bigTextStyle(Context self, @DelegatesTo(NotificationCompat.BigTextStyle) Closure styleSpec) {
        def bigStyle = new NotificationCompat.BigTextStyle()
        bigStyle.with(styleSpec)
        bigStyle
    }

    static Intent intent(Context self, Class<?> clazz) {
        new Intent(self, clazz)
    }

    static void startActivity(Context self,
                              Class<? extends Activity> activity,
                              @DelegatesTo(value=Intent, strategy = Closure.DELEGATE_FIRST) Closure intentSpec) {
        def intent = new Intent(self, activity)
        def clone = (Closure) intentSpec.clone()
        clone.resolveStrategy = Closure.DELEGATE_FIRST
        clone.delegate = intent
        clone()
        self.startActivity(intent)
    }

}