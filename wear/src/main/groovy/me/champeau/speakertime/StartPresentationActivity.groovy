package me.champeau.speakertime

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import com.google.android.gms.wearable.Wearable
import groovy.transform.CompileStatic
import me.champeau.speakertime.support.GoogleApiProvider
import me.champeau.speakertime.support.MessageConstants

@CompileStatic
class StartPresentationActivity extends Activity implements GoogleApiProvider {
    private static final int SPEECH_REQUEST_CODE = 0

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        displaySpeechRecognizer()
    }

// Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

// This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS)
            String spokenText = results.get(0)
            if (spokenText.endsWith(' minutes')) {
                String minutes = spokenText - ' minutes'
                startActivityOnHandheld(minutes)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
        finish()
    }

    private void startActivityOnHandheld(String duration) {
        Thread.start {
            createGoogleApi()
            connectGoogleApi()
            def nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient)
            def ids = nodes.await().nodes*.id
            ids.each { n ->
                def result = Wearable.MessageApi.sendMessage(
                        googleApiClient, n, MessageConstants.START_HANDHELD_ACTIVITY, duration.bytes).await()
                if (!result.status.success) {
                    Log.e("GoogleAplProvider", "ERROR: failed to send Message: ${result.status}")
                }
            }
            disconnectGoogleApi()
        }
    }
}