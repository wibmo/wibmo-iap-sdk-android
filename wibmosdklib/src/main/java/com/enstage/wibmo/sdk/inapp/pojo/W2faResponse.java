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
package com.enstage.wibmo.sdk.inapp.pojo;

/**
 * Created by akshath on 27/10/14.
 */
public class W2faResponse extends GenericResponse {
    private String wibmoTxnId;
    private String msgHash;
    private String dataPickUpCode;

    private String merAppData = "";
    private String merTxnId;

    public String getMsgHash() {
        return msgHash;
    }

    public void setMsgHash(String msgHash) {
        this.msgHash = msgHash;
    }

    public String getDataPickUpCode() {
        return dataPickUpCode;
    }

    public void setDataPickUpCode(String dataPickUpCode) {
        this.dataPickUpCode = dataPickUpCode;
    }

    public String getWibmoTxnId() {
        return wibmoTxnId;
    }

    public void setWibmoTxnId(String wibmoTxnId) {
        this.wibmoTxnId = wibmoTxnId;
    }

    public String getMerAppData() {
        return merAppData;
    }

    public void setMerAppData(String merAppData) {
        this.merAppData = merAppData;
    }

    public String getMerTxnId() {
        return merTxnId;
    }

    public void setMerTxnId(String merTxnId) {
        this.merTxnId = merTxnId;
    }
}
