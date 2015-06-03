/*
 * Copyright (C) 2014 enStage Inc. Cupertino, California USA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enstage.wibmo.util;

import android.content.Context;
import android.util.Log;

import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Created by akshath on 21/10/14.
 */
public class HttpUtil {
    private static final String TAG = "wibmo.sdk.HttpUtil";

    //ok http
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType WWW_FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();
    private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10 MiB
    private static Cache cache = null;
    private static boolean okhttpinit = false;

    public static void init(Context context) {
        //client = new OkHttpClient();
        if(okhttpinit==false || cache ==null) {
            cache = createHttpClientCache(context);
            client.setCache(cache);

            setSSLstuff();

            okhttpinit = true;
        }
    }

    private static void setSSLstuff() {
        //30sec,90sec TODO
        client.setConnectTimeout(30, TimeUnit.SECONDS);
        client.setWriteTimeout(30, TimeUnit.SECONDS);
        client.setReadTimeout(90, TimeUnit.SECONDS);

        try {
            makeSSLSocketFactory();
            client.setSslSocketFactory(sslSocketFactory);
            client.setHostnameVerifier(vf);
        } catch (Exception e) {
            Log.e(TAG, "Error "+e,e);
        }
    }

    public static Cache createHttpClientCache(Context context) {
        File cacheDir = context.getDir("service_api_cache", Context.MODE_PRIVATE);
        return new Cache(cacheDir, HTTP_CACHE_SIZE);
    }

    public static String postData(String posturl, byte postData[], boolean useCache,
                                  MediaType mediaType) throws Exception {
        String data = new String(postData, "utf-8");
        int i = data.indexOf("p=");
        int j = data.indexOf("&", i);

        String method = "NA";

        if(i!=-1 && j!=-1) {
            method = data.substring(i+2, j);
            data = null;
        }

        Log.i(TAG, "op: " + method + " @ " + posturl);

        return postDataUseOkHttp(posturl, postData, useCache, mediaType);
    }


    public static String postDataUseOkHttp(String posturl, byte postData[],
                                           boolean useCache, MediaType mediaType) throws Exception {
        URL url;
        long stime = System.currentTimeMillis();
        try {
            url = new URL(posturl);

            RequestBody body = RequestBody.create(mediaType, postData);

            Request.Builder builder = new Request.Builder();
            builder.url(url);
            if(useCache==false) {
                builder.addHeader("Cache-Control", "no-cache");
            }
            builder.post(body);
            Request request = builder.build();

            if(okhttpinit==false) {
                Log.w(TAG, "WibmoSDK init was false; "+client.getSslSocketFactory());

                if(client.getSslSocketFactory()==null) {
                    setSSLstuff();
                }
            }

            Response res = client.newCall(request).execute();

            // Read the response.
            if (res.code() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Bad res code: "+res.code());
                Log.e(TAG, "Url was: "+posturl.toString());
                Log.e(TAG, "HTTP response: "+res.message()+"; "+res.body().string());
                return null;
            }

            return res.body().string();
        } finally {
            long etime = System.currentTimeMillis();
            Log.i(TAG, "time dif: "+(etime-stime));
        }
    }

    private static SSLSocketFactory sslSocketFactory;

    private static TrustManager[] trustManager;
    private static SSLContext sslContext;

    public static void makeSSLSocketFactory() throws Exception {
        if (sslContext == null && getSslSocketFactory() == null) {
            Log.d(TAG, "making makeSSLSocketFactory");
            sslContext = SSLContext.getInstance("TLS");
            if (trustManager == null) {
                if(WibmoSDKConfig.isTestMode()) {
                    trustManager = new TrustManager[]{DummyTrustManager.getInstance()};
                }
            }

            sslContext.init(new KeyManager[0], trustManager, new SecureRandom());
            Log.d(TAG, "done makeSSLSocketFactory");
        }

        if (getSslSocketFactory() == null) {
            Log.d(TAG, "making getSocketFactory");
            sslSocketFactory = sslContext.getSocketFactory();
            Log.d(TAG, "done getSocketFactory");
        }
    }
    static HostnameVerifier vf = new HostnameVerifier() {
        public boolean verify(String hostName, SSLSession session) {
            //logger.warning("WARNING: hostname may not match the certificate host name :" + hostName);
            return true;
        }
    };

    public static SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public static void setSslSocketFactory(SSLSocketFactory _sslSocketFactory) {
        sslSocketFactory = _sslSocketFactory;
    }

}
