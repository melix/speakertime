package me.champeau.speakertime

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.wearable.MessageApi
import com.google.android.gms.wearable.NodeApi
import com.google.android.gms.wearable.Wearable
import groovy.transform.CompileStatic
import me.champeau.speakertime.support.GoogleApiProvider
import me.champeau.speakertime.support.InjectViews
import me.champeau.speakertime.support.MessageConstants
import me.champeau.speakertime.support.ViewById
import reactor.core.Environment
import reactor.function.Consumer
import reactor.rx.action.Action
import reactor.rx.spec.Streams
import com.google.android.gms.wearable.Node

@CompileStatic
@InjectViews
class PresentationActivity extends Activity implements GoogleApiProvider {

    private final Environment env = new Environment()

    @ViewById(R.id.timer)
    private TextView timerView

    @ViewById(R.id.start_button)
    private Button startButton

    @ViewById(R.id.stop_button)
    private Button stopButton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presentation)

        createGoogleApi()
        def timer = new PresenterTimer()
        timer.onTick {
            String duration = convertToDuration(it)
            def api = googleApiClient
            Thread.start {
                Wearable.NodeApi.getConnectedNodes(api).await().nodes.each {
                    def result = Wearable.MessageApi.sendMessage(
                            api, it.id, MessageConstants.MSG_TIME_LEFT, duration.bytes).await()
                    if (!result.status.success) {
                        Log.e("PresentationActivity", "ERROR: failed to send Message: ${result.status}")
                    }
                }
            }
            runOnUiThread {
                timerView.setText(duration)
            }
        }
        injectViews()
        startButton.onClickListener = {
            timer.start()
        }
        stopButton.onClickListener = {
            timer.cancel()
        }
        //startActivity new Intent(this, ReactiveActivity)

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.presentation, menu)
        true
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.itemId
        if (id == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private static String convertToDuration(long millis) {
        long seconds = (long) millis / 1000L
        long s = seconds % 60
        long m = ((long) (seconds / 60)) % 60
        long h = ((long) (seconds / 3600L)) % 24
        "$h:$m:$s"
    }


}
