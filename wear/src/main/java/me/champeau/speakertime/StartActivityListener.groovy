package me.champeau.speakertime

import android.content.Intent
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import me.champeau.speakertime.support.MessageConstants

class StartActivityListener extends WearableListenerService {
    @Override
    void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent)
        if (messageEvent.path == MessageConstants.START_WEAR_ACTIVITY) {
            def intent = new Intent(this, PresentationActivity)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}