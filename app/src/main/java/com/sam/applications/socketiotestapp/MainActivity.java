package com.sam.applications.socketiotestapp;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    private WebSocketClient mWebSocketClient;
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");

            final InputStream in = getResources().openRawResource(
                    R.raw.test);
            trustStore.load(in, null);

            final TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            final SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, trustAllCerts,
                    new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx
                    .getSocketFactory());
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException |
                CertificateException |
                KeyManagementException e) {
            e.printStackTrace();
        }

        tvMessage = (TextView) findViewById(R.id.tvMessage);
        tvMessage.setText("Connecting...");
        Log.i("Websocket", "Instanciando websocket");
        URI uri;
        try {
            uri = new URI("wss://echo.websocket.org");

        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri, new Draft_17()) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "onOpen: " + serverHandshake.getHttpStatus() + " " + serverHandshake.getHttpStatusMessage());
                tvMessage.setText(tvMessage.getText()
                        + "\nonOpen: " + serverHandshake.getHttpStatus() + " " + serverHandshake
                        .getHttpStatusMessage());
            }

            @Override
            public void onMessage(String s) {
                Log.i("Websocket", "onMessage: " + s);
                tvMessage.setText(tvMessage.getText()
                        + "\nonMessage: " + s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                tvMessage.setText(tvMessage.getText()
                        + "\nonClose: " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "onError: " + e.getMessage());
                tvMessage.setText(tvMessage.getText()
                        + "\nonError: " + e.getMessage());
            }
        };

        try {
            mWebSocketClient.setSocket(HttpsURLConnection.getDefaultSSLSocketFactory().createSocket());
            mWebSocketClient.connectBlocking();

            /*int port = 443;
            mWebSocketClient.setSocket(SSLSocketFactory.getDefault().createSocket(uri.getHost(), port));
            mWebSocketClient.connectBlocking();*/
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    }};
}
