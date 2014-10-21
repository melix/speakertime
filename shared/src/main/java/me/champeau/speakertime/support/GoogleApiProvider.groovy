package me.champeau.speakertime.support

import android.content.Context
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.MessageApi
import com.google.android.gms.wearable.Wearable
import groovy.transform.CompileStatic

@CompileStatic
trait GoogleApiProvider
{
    GoogleApiClient googleApiClient

    Object me() { this }

    void createGoogleApi() {
        googleApiClient = new GoogleApiClient.Builder((Context) me())
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