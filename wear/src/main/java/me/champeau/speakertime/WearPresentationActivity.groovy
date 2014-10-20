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
    private int lastElapsed = -1

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
                int elapsed = Integer.valueOf(data[1])

                mTextView.text = timeLeft
                timeleftView.elapsedPercent = elapsed
                if (lastElapsed!=((int) (elapsed/10))) {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE)
                    vibrator.vibrate(Math.min(200, 20*elapsed))
                }
                lastElapsed = (int) elapsed/10
            }
        }
        //Log.d("PresentationActivityWear", "Received message: Path ${messageEvent.path}")
    }
}
