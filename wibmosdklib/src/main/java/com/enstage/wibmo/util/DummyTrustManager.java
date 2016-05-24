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

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


//Google does not allow Dummy Trust Manager any more as of May 17, 2016
//https://support.google.com/faqs/answer/6346016
/*
public class DummyTrustManager implements X509TrustManager {

    private static DummyTrustManager instance;

    static {
        try {
            instance = new DummyTrustManager();
        } catch (Throwable e) {
        }
    }
    private X509TrustManager sunJSSEX509TrustManager;

    public static DummyTrustManager getInstance() {
        return instance;
    }

    public DummyTrustManager() throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        KeyStore ks = null;
        tmf.init((KeyStore) null);

        TrustManager tms[] = tmf.getTrustManagers();
        for (int i = 0; i < tms.length; i++) {
            if (tms[i] instanceof X509TrustManager) {
                sunJSSEX509TrustManager = (X509TrustManager) tms[i];
                break;
            }
        }
        if (sunJSSEX509TrustManager == null) {
            throw new Exception("Couldn't initialize");
        }
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        try {
            sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
        } catch (CertificateException excep) {
            // do any special handling here, or rethrow exception.
            throw excep;
        }
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        try {
            sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException excep) {
			//Possibly pop up a dialog box asking whether to trust the
			//cert chain.
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        // return sunJSSEX509TrustManager.getAcceptedIssuers();
        if (sunJSSEX509TrustManager == null) {
            return null;
        }

        return sunJSSEX509TrustManager.getAcceptedIssuers();
    }
}*/
