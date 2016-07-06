package com.enstage.wibmo.util;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Akshathkumar Shetty on 23/05/16.
 */
public class SSLUtil {
    private static final String TAG = "wibmo.sdk.SSLUtil";

    public static TrustManager[] loadTrustManagerDefault(Context context) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        TrustManager[] trustManager = trustManagerFactory.getTrustManagers();
        if (trustManager.length != 1 || !(trustManager[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManager));
        }

        return trustManager;
    }

    public static TrustManager[] loadTrustManagerFromRawBks(Context context, int resId, char pwd[])
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        InputStream in = null;
        try {
            KeyStore trusted = KeyStore.getInstance("BKS");

            Log.v(TAG, "Loading trust cert.." + context.getResources().getResourceName(resId));
            in = context.getResources().openRawResource(resId);
            trusted.load(in, pwd);

            Enumeration<String> enumeration = trusted.aliases();
            while(enumeration.hasMoreElements()) {
                Log.v(TAG, "ca alias: " + enumeration.nextElement());
            }

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trusted);

            TrustManager[] trustManager = trustManagerFactory.getTrustManagers();
            if (trustManager.length != 1 || !(trustManager[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManager));
            }

            return trustManager;
        } finally {
            if(in!=null) {
                in.close();
            }
        }
    }
}
