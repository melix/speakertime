package me.champeau.speakertime

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.google.android.gms.wearable.Wearable
import groovy.transform.CompileStatic
import me.champeau.speakertime.support.GoogleApiProvider
import me.champeau.speakertime.support.MessageConstants

@CompileStatic
public class CountDownService extends Service implements GoogleApiProvider {
    public static String START_TIMER = "start"
    public static String STOP_TIMER = "stop"
    public static String TICK = "tick"
    public static String TIME_LEFT = "timeleft"

    private PresenterTimer timer
    private Set<String> nodeIds = []

    int totalDuration = 45 * 60 * 1000

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        void onReceive(Context context, Intent intent) {
            onHandleIntent(intent)
        }
    }

    @Override
    void onCreate() {
        super.onCreate()
        createGoogleApi()
        connectGoogleApi()
        def nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient)
        nodes.resultCallback = {
            nodeIds.addAll(it.nodes*.id)
        }

        def filter = new IntentFilter()
        filter.addAction(START_TIMER)
        filter.addAction(STOP_TIMER)
        registerReceiver(receiver, filter)
    }

    @Override
    void onDestroy() {
        super.onDestroy()
        disconnectGoogleApi()
        unregisterReceiver(receiver)
    }

    protected void onHandleIntent(Intent intent) {
        switch (intent.action) {
            case START_TIMER:
                if (timer) {
                    timer.cancel()
                    cancelNotification()
                }
                timer = new PresenterTimer(totalDuration)
                timer.onTick {
                    String duration = Utils.convertToDuration(it)
                    double elapsed = (double) (100d * (totalDuration - it) / totalDuration)

                    Thread.start {
                        nodeIds.each { n ->
                            def result = Wearable.MessageApi.sendMessage(
                                    googleApiClient, n, MessageConstants.MSG_TIME_LEFT, "$duration;$elapsed".bytes).await()
                            if (!result.status.success) {
                                Log.e("GoogleAplProvider", "ERROR: failed to send Message: ${result.status}")
                            }
                        }
                    }
                }
                timer.onTick {
                    def resultIntent = new Intent(TICK)
                    resultIntent.putExtra(TIME_LEFT, it)
                    sendBroadcast(resultIntent)
                }
                timer.start()
                break
            case STOP_TIMER:
                if (timer) {
                    timer.cancel()
                }
                cancelNotification()
                break
        }
    }

    private void cancelNotification() {
        Thread.start {
            nodeIds.each { n ->
                def result = Wearable.MessageApi.sendMessage(
                        googleApiClient, n, MessageConstants.STOP_WEAR_ACTIVITY, MessageConstants.EMPTY_MESSAGE).await()
                if (!result.status.success) {
                    Log.e("GoogleAplProvider", "ERROR: failed to send Message: ${result.status}")
                }
            }
        }
    }

    @Override
    IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException()
    }
}
