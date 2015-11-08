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
 *
 * @author Preetham
 */
public class W2faInitResponse extends GenericResponse {
    private static final long serialVersionUID = 1L;

	private boolean wibmoCustomer;
	private String registeredUas;
	private MerchantInfo merchantInfo;
    private TransactionInfo transactionInfo;

	private String wibmoTxnId;
	private String wibmoTxnToken;
	private String webUrl;
	private String msgHash;

	public String getWibmoTxnId() {
		return wibmoTxnId;
	}

	public void setWibmoTxnId(String wibmoTxnId) {
		this.wibmoTxnId = wibmoTxnId;
	}

	public String getWibmoTxnToken() {
		return wibmoTxnToken;
	}

	public void setWibmoTxnToken(String wibmoTxnToken) {
		this.wibmoTxnToken = wibmoTxnToken;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}

	public String getMsgHash() {
		return msgHash;
	}

	public void setMsgHash(String msgHash) {
		this.msgHash = msgHash;
	}

	public MerchantInfo getMerchantInfo() {
		return merchantInfo;
	}

	public void setMerchantInfo(MerchantInfo merchantInfo) {
		this.merchantInfo = merchantInfo;
	}

	public boolean isWibmoCustomer() {
		return wibmoCustomer;
	}

	public void setWibmoCustomer(boolean wibmoCustomer) {
		this.wibmoCustomer = wibmoCustomer;
	}

	public String getRegisteredUas() {
		return registeredUas;
	}

	public void setRegisteredUas(String registeredUas) {
		this.registeredUas = registeredUas;
	}

    public TransactionInfo getTransactionInfo() {
        return transactionInfo;
    }

    public void setTransactionInfo(TransactionInfo transactionInfo) {
        this.transactionInfo = transactionInfo;
    }
}
