package me.champeau.speakertime.support

import android.content.Context
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.MessageApi
import com.google.android.gms.wearable.Wearable
import groovy.transform.CompileStatic
import groovy.transform.SelfType

@CompileStatic
@SelfType(Context)
trait GoogleApiProvider {
    GoogleApiClient googleApiClient

    void createGoogleApi() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build()

    }

    void connectGoogleApi() {
        if (googleApiClient) {
            googleApiClient.connect()
            if (this instanceof MessageApi.MessageListener) {
                Wearable.MessageApi.addListener(googleApiClient, this)
            }
        }
    }

    void disconnectGoogleApi() {
        if (googleApiClient && googleApiClient.connected) {
            if (this instanceof MessageApi.MessageListener) {
                Wearable.MessageApi.removeListener(googleApiClient, this)
            }
            googleApiClient.disconnect()
        }
    }
}