package me.champeau.speakertime

import android.app.Activity
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.view.WatchViewStub
import android.widget.TextView
import com.google.android.gms.wearable.MessageApi
import com.google.android.gms.wearable.MessageEvent
import groovy.transform.CompileStatic
import me.champeau.speakertime.support.GoogleApiProvider
import me.champeau.speakertime.support.MessageConstants

@CompileStatic
class WearPresentationActivity extends Activity implements GoogleApiProvider, MessageApi.MessageListener {

    private TextView mTextView
    private TimeLeftView timeleftView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentation)
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub)
        stub.onLayoutInflatedListener = {
            mTextView = (TextView) stub.findViewById(R.id.text)
            timeleftView = (TimeLeftView) stub.findViewById(R.id.timeleftView)
        }
        createGoogleApi()
    }

    @Override
    protected void onStart() {
        super.onStart()
        connectGoogleApi()
    }

    @Override
    protected void onStop() {
        super.onStop()
        disconnectGoogleApi()
    }

    @Override
    void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.path == MessageConstants.MSG_TIME_LEFT) {
            runOnUiThread {
                String[] data = new String(messageEvent.data).split(';')
                String timeLeft = data[0]
                double elapsed = Double.valueOf(data[1])

                mTextView.text = timeLeft
                timeleftView.elapsedPercent = elapsed
            }
        }
    }
}
