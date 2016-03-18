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

import java.io.Serializable;


/**
 *
 * @author Preetham
 */
public class TransactionInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String txnAmount;
	private String txnCurrency;
	private String txnDesc;
	private String merAppData = "";
	private String merTxnId;
	private String[] supportedPaymentType;
	private String[] restrictedPaymentType;
	private String txnFormattedAmount;

    private String merDataField;

	private String txnDate;//YYYYMMDD


	private boolean txnAmtKnown = true;
	private boolean chargeLater = false;


	public String getTxnAmount() {
		return txnAmount;
	}

	public void setTxnAmount(String txnAmount) {
		this.txnAmount = txnAmount;
	}

	public String getTxnCurrency() {
		return txnCurrency;
	}

	public void setTxnCurrency(String txnCurrency) {
		this.txnCurrency = txnCurrency;
	}

	public String getTxnDesc() {
		return txnDesc;
	}

	public void setTxnDesc(String txnDesc) {
		this.txnDesc = txnDesc;
	}

	public String[] getSupportedPaymentType() {
		return supportedPaymentType;
	}

	public void setSupportedPaymentType(String[] supportedPaymentType) {
		this.supportedPaymentType = supportedPaymentType;
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

    public String getMerDataField() {
        return merDataField;
    }

    public void setMerDataField(String merDataField) {
        this.merDataField = merDataField;
    }


	public String getTxnFormattedAmount() {
		return txnFormattedAmount;
	}

	public void setTxnFormattedAmount(String txnFormattedAmount) {
		this.txnFormattedAmount = txnFormattedAmount;
	}

	public String[] getRestrictedPaymentType() {
		return restrictedPaymentType;
	}

	public void setRestrictedPaymentType(String[] restrictedPaymentType) {
		this.restrictedPaymentType = restrictedPaymentType;
	}

	public String getTxnDate() {
		return txnDate;
	}

	public void setTxnDate(String txnDate) {
		this.txnDate = txnDate;
	}


	public boolean isTxnAmtKnown() {
		return txnAmtKnown;
	}

	public void setTxnAmtKnown(boolean txnAmtKnown) {
		this.txnAmtKnown = txnAmtKnown;
	}

	public boolean isChargeLater() {
		return chargeLater;
	}

	public void setChargeLater(boolean chargeLater) {
		this.chargeLater = chargeLater;
	}
}
