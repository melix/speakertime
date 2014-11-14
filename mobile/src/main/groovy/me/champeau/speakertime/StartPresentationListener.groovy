package me.champeau.speakertime

import android.content.Intent
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import groovy.transform.CompileStatic
import me.champeau.speakertime.support.MessageConstants

@CompileStatic
class StartPresentationListener extends WearableListenerService {
    @Override
    void onMessageReceived(MessageEvent messageEvent) {
        if (MessageConstants.START_HANDHELD_ACTIVITY==messageEvent.path) {
            startActivity(PresentationActivity) {
                def durationAsString = new String(messageEvent.data).trim()
                putExtra(PresentationActivity.DURATION, 60000*Long.valueOf(durationAsString))
                addFlags(FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}