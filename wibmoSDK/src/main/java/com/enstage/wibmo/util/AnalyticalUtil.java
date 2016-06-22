package com.enstage.wibmo.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Akshathkumar Shetty on 30/05/16.
 */
public class AnalyticalUtil {


    public static String buildEventToPostFor(String eventName) throws JSONException {
        JSONObject dataObj = new JSONObject();
        dataObj.put("event", eventName);

        return null;
    }
}
