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

import com.enstage.wibmo.sdk.R;
import com.enstage.wibmo.sdk.WibmoSDKConfig;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import okhttp3.Cache;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by akshath on 21/10/14.
 */
public class HttpUtil {
    private static final String TAG = "wibmo.sdk.HttpUtil";

    //ok http
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType WWW_FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private static final int HTTP_CACHE_SIZE = 20 * 1024 * 1024; // 20 MiB
    private static boolean okhttpinit = false;

    private static OkHttpClient client = null;
    private static Cache cache = null;

    private static SSLSocketFactory sslSocketFactory;

    private static TrustManager[] trustManager;
    private static SSLContext sslContext;

    private static HostnameVerifier vf = null;

    private static void makeHostnameVerifier() {
        vf = new HostnameVerifier() {
            public boolean verify(String hostName, SSLSession session) {
                //logger.warning("WARNING: hostname may not match the certificate host name :" + hostName);
                return true;
            }
        };
    }

    public static boolean init(Context context) {
        if(okhttpinit==false || cache ==null) {
            try {
                File cacheDirectory = context.getDir("service_api_cache", Context.MODE_PRIVATE);
                cache = new Cache(cacheDirectory, HTTP_CACHE_SIZE);
                makeSSLSocketFactory(context);

                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .cache(cache)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(90, TimeUnit.SECONDS)
                        .sslSocketFactory(sslSocketFactory);

                if(WibmoSDKConfig.isTestMode()) {
                    makeHostnameVerifier();
                    builder.hostnameVerifier(vf);
                }

                client = builder.build();
                okhttpinit = true;
            } catch (Exception e) {
                Log.e(TAG, "Error "+e,e);
                okhttpinit = false;
            }
        }
        return okhttpinit;
    }

    public static String postData(String posturl, byte postData[], boolean useCache,
                                  MediaType mediaType) throws Exception {
        return postData(posturl, postData, useCache, mediaType, null);
    }

    public static String postData(String posturl, byte postData[], boolean useCache,
                                  MediaType mediaType, Map<String,String> headers) throws Exception {
        String data = new String(postData, "utf-8");
        int i = data.indexOf("p=");
        int j = data.indexOf("&", i);

        String method = "NA";

        if(i!=-1 && j!=-1) {
            method = data.substring(i+2, j);
            data = null;
        }

        Log.i(TAG, "op: " + method + " @ " + posturl);

        return postDataUseOkHttp(posturl, postData, useCache, mediaType, headers);
    }


    private static String postDataUseOkHttp(String posturl, byte postData[],
                                            boolean useCache, MediaType mediaType, Map<String,String> headers) throws Exception {
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

            if(headers!=null) {
                Iterator<String> iterator = headers.keySet().iterator();
                String headerKey = null;
                while(iterator.hasNext()) {
                    headerKey = iterator.next();
                    builder.addHeader(headerKey, headers.get(headerKey));
                }
            }

            Request request = builder.build();

            if(okhttpinit==false) {
                Log.w(TAG, "WibmoSDK okhttpinit was false;");
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

    public static void makeSSLSocketFactory(Context context) throws Exception {
        if (sslContext == null && getSslSocketFactory() == null) {
            Log.d(TAG, "making makeSSLSocketFactory.. ");
            sslContext = SSLContext.getInstance("TLS");
            if (trustManager == null) {
                if(WibmoSDKConfig.isTestMode()) {
                    Log.v(TAG, "Loading non uat trust cert..");
                    trustManager = SSLUtil.loadTrustManagerFromRawBks(context,
                            R.raw.trust_wsdk_bks_star_ens_uat, ("pa"+"ssw"+"ord").toCharArray());
                } else {
                    Log.v(TAG, "using default null trust manager");
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

    public static SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public static void setSslSocketFactory(SSLSocketFactory _sslSocketFactory) {
        sslSocketFactory = _sslSocketFactory;
    }

}
