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
package com.enstage.wibmo.sdk.inapp;

import android.util.Log;

import com.enstage.wibmo.sdk.WibmoSDKConfig;
import com.enstage.wibmo.sdk.inapp.pojo.IAPaymentStatusResponse;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.W2faInitResponse;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitRequest;
import com.enstage.wibmo.sdk.inapp.pojo.WPayInitResponse;
import com.enstage.wibmo.util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by akshath on 21/10/14.
 */
public class InAppHandler {
    private static final String TAG = InAppHandler.class.getSimpleName();

    //private static ObjectMapper jacksonMapper = new ObjectMapper();
    private static Gson gson = InAppUtil.makeGson();

    public static W2faInitResponse init2FA(W2faInitRequest request) throws Exception {
        try {
            Log.d(TAG, "init");
            String posturl = WibmoSDKConfig.getInit2FAPostUrl();

            String postData = gson.toJson(request); //request.toJSON();
            //Log.v(TAG, "postData: "+postData);

            String rawres = HttpUtil.postData(posturl,
                    postData.getBytes(WibmoSDKConfig.CHARTSET),
                    false, HttpUtil.JSON, null);
            //Log.v(TAG, "rawres: "+rawres);

            if (rawres == null) {
                throw new IOException("Unable to do init2FA!");
            }

            //return jacksonMapper.readValue(rawres, W2faInitResponse.class);
            return gson.fromJson(rawres, W2faInitResponse.class);
        } catch (Exception e) {
            Log.e(TAG, "Error: "+e, e);
            throw e;
        }
    }

    public static WPayInitResponse initPay(WPayInitRequest request) throws Exception {
        try {
            Log.d(TAG, "init");
            String posturl = WibmoSDKConfig.getInitPayPostUrl();

            String postData = gson.toJson(request); //request.toJSON();
            Log.v(TAG, "postData: "+postData);

            String rawres = HttpUtil.postData(posturl,
                    postData.getBytes(WibmoSDKConfig.CHARTSET),
                    false, HttpUtil.JSON, null);
            Log.v(TAG, "rawres: "+rawres);

            if (rawres == null) {
                throw new IOException("Unable to do initPay!");
            }

            //return jacksonMapper.readValue(rawres, WPayInitResponse.class);
            return gson.fromJson(rawres, WPayInitResponse.class);
        } catch (Exception e) {
            Log.e(TAG, "Error: "+e, e);
            throw e;
        }
    }

    public static IAPaymentStatusResponse checkIAPStatus(W2faInitRequest request, W2faInitResponse response) throws Exception {
        try {
            Log.d(TAG, "checkIAPStatus");
            String posturl = WibmoSDKConfig.getStatusIAPPostUrl() + response.getWibmoTxnId();

            String postData = gson.toJson(request); //request.toJSON();
            Log.v(TAG, "postData: "+postData);

            Map<String, String> headers = new Hashtable<>();
            headers.put("X-IAP-Token", response.getWibmoTxnToken());

            String rawres = HttpUtil.postData(posturl,
                    postData.getBytes(WibmoSDKConfig.CHARTSET),
                    false, HttpUtil.JSON, headers);
            Log.v(TAG, "rawres: "+rawres);

            if (rawres == null) {
                throw new IOException("Unable to do checkIAPStatus!");
            }

            //return jacksonMapper.readValue(rawres, WPayInitResponse.class);
            return gson.fromJson(rawres, IAPaymentStatusResponse.class);
        } catch (Exception e) {
            Log.e(TAG, "Error: "+e, e);
            throw e;
        }
    }
}
