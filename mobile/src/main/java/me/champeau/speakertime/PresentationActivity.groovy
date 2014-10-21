package me.champeau.speakertime

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
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
import com.google.android.gms.wearable.Node

@CompileStatic
@InjectViews
class PresentationActivity extends Activity {

    public static final String DURATION = "duration"

    private final Environment env = new Environment()

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        void onReceive(Context context, Intent intent) {
            if (intent.action==CountDownService.TICK) {
                String duration = Utils.convertToDuration(intent.getLongExtra(CountDownService.TIME_LEFT,0))
                runOnUiThread {
                    timerView.text = duration
                }
            }
        }
    }

    @ViewById(R.id.timer)
    private TextView timerView

    @ViewById(R.id.start_button)
    private Button startButton

    @ViewById(R.id.stop_button)
    private Button stopButton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        contentView = R.layout.activity_presentation
        def startServiceIntent = new Intent(this, CountDownService)

        def duration = intent?.getLongExtra(DURATION, 0)
        if (duration) {
            startServiceIntent.putExtra(DURATION, duration)
        }

        startService(startServiceIntent)
        injectViews()

        startButton.onClickListener = {
            def st = new Intent()
            st.action = CountDownService.START_TIMER
            sendBroadcast(st)
        }
        stopButton.onClickListener = {
            def st = new Intent()
            st.action = CountDownService.STOP_TIMER
            sendBroadcast(st)
        }

        registerReceiver(receiver, new IntentFilter(CountDownService.TICK))

        if (duration) {
            def st = new Intent()
            st.action = CountDownService.START_TIMER
            sendBroadcast(st)
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
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
}
