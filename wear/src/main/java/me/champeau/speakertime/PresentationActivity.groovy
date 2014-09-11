package me.champeau.speakertime

import android.app.Activity
import android.os.Bundle
import android.support.wearable.view.WatchViewStub
import android.util.Log
import android.widget.TextView
import com.google.android.gms.wearable.MessageApi
import com.google.android.gms.wearable.MessageEvent
import groovy.transform.CompileStatic
import me.champeau.speakertime.support.GoogleApiProvider
import me.champeau.speakertime.support.MessageConstants

@CompileStatic
class PresentationActivity extends Activity implements GoogleApiProvider, MessageApi.MessageListener {

    private TextView mTextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentation)
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub)
        stub.onLayoutInflatedListener = {
            mTextView = (TextView) stub.findViewById(R.id.text)
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
                mTextView.setText(new String(messageEvent.data))
            }
        }
        //Log.d("PresentationActivityWear", "Received message: Path ${messageEvent.path}")
    }
}
