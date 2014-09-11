package me.champeau.speakertime

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.java_websocket.client.DefaultSSLWebSocketClientFactory
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.SimpleLocationOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import reactor.core.Environment
import reactor.rx.Stream
import reactor.rx.spec.Streams

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

@CompileStatic
class ReactiveActivity extends Activity {

    public static
    final ItemizedIconOverlay.OnItemGestureListener DEFAULT_LISTENER = new ItemizedIconOverlay.OnItemGestureListener() {
        @Override
        boolean onItemSingleTapUp(int index, Object o) {
            true
        }

        @Override
        boolean onItemLongPress(int index, Object o) {
            true
        }
    }

    private WebSocketClient mWebSocketClient
    MapView view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        view = (MapView) findViewById(R.id.mapview)
        view.tileSource = TileSourceFactory.MAPNIK
        view.builtInZoomControls = true
        view.multiTouchControls = true
        view.useDataConnection = true

        def controller = view.controller
        def root = new GeoPoint(32.775987d,-96.808031d) // Dallas, TX
        controller.center = root
        controller.zoom = 16
        controller.animateTo(root)
        def myLocation = new SimpleLocationOverlay(this);
        def compass = new CompassOverlay(this, new InternalCompassOrientationProvider(this),
                view);
        view.overlays.add(myLocation)
        view.overlays.add(compass)

        compass.enableCompass()

        Thread.start { connectWebSocket() }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.reactive, menu)
        return true
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

    void connectWebSocket() {
        def stream = Streams.defer(0d)

        stream.map{
            whereAmIRequestId(0d,0d)
        }.flatMap{ String it ->
            websocketConnection(it)
        }
        .consume { OverlayItem item ->
            view.overlays.add(new ItemizedIconOverlay(this, [item], DEFAULT_LISTENER))
            view.controller.animateTo(item.point)
        }
        .when (Throwable) {
            Log.d("Websocket", it.toString(), (Throwable)it)
        }
        Log.d("Websocket", stream.debug().toString())

    }

    Stream<?> websocketConnection(String id){
        def stream = Streams.defer()
        def tail = stream.map { String m ->
            def json = (Map) new JsonSlurper().parse(m.bytes)
            def coord = (List) json.coordinates
            double longitude = (double) coord[0]
            double latitude = (double) coord[1]
            new Gr8People(id:(String)json.id, name: (String)json.name, longitude: longitude, latitude: latitude)
        }
        .map { Gr8People it ->
            def point = new GeoPoint(it.latitude, it.longitude)
            OverlayItem item = new OverlayItem(it.id, it.name, point)
            item.marker = resources.getDrawable(R.drawable.ic_action_place)
            item
        }

        URI uri = new URI("wss://geocoder.cfapps.io:4443/location/$id/nearby?distance=20")
        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.d("Websocket", "Opened")
                //mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL)
            }

            @Override
            public void onMessage(String s) {
                Log.i("Websocket", "OnMessage: $s")
                stream.broadcastNext(s)
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.d("Websocket", "Closed $s")
                stream.broadcastComplete()
                connectWebSocket()
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error ${e.message}")
                stream.broadcastError(e)
            }
        }
        trustAllHosts(mWebSocketClient)
        mWebSocketClient.connect()
        tail
    }

    String whereAmIRequestId(double longitude, double latitude) {
        def httpClient = new DefaultHttpClient()
        def httpPost = new HttpPost("https://geocoder.cfapps.io/location")
        def jsonSrc = """{
"name":"Cedric on Android, Pivotal",
"address": "Omni Hotel",
"city": "Dallas",
"province": "TX",
"coordinates": [-96.808031,32.775987]
}
"""
        httpPost.addHeader("content-type", "application/json")
        httpPost.setEntity(new StringEntity(jsonSrc))
        def response = httpClient.execute(httpPost)
        def json = (Map) new JsonSlurper().parse(response.entity.content)
        Log.d("Websocket", json.toString())
        json.id
    }

    private static void trustAllHosts(WebSocketClient appClient) {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = [new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { [] as X509Certificate[] }


            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }


            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        }
        ]

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            appClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sc));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Gr8People {
        String id
        String name
        double latitude
        double longitude
    }
}
