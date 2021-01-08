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

public class W2faInitRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String version = "2";//iapv2

	private MerchantInfo merchantInfo;
	private TransactionInfo transactionInfo;
	private CustomerInfo customerInfo;
	private CardFee cardFee;

	private DeviceInfo deviceInfo;
	private String msgHash;
	private String merchantReturnUrl;

	private CardInfo cardInfo;
	private String txnType;

	private boolean billingAddressRequired;
	private boolean shippingAddressRequired;
	private boolean profileRequired;
	private boolean promptPromoCode;

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	/*Category id for decoupled promo code changes*/
	private int categoryId;

	public MerchantInfo getMerchantInfo() {
		return merchantInfo;
	}

	public void setMerchantInfo(MerchantInfo merchantInfo) {
		this.merchantInfo = merchantInfo;
	}

	public TransactionInfo getTransactionInfo() {
		return transactionInfo;
	}

	public void setTransactionInfo(TransactionInfo transactionInfo) {
		this.transactionInfo = transactionInfo;
	}

	public CustomerInfo getCustomerInfo() {
		return customerInfo;
	}

	public void setCustomerInfo(CustomerInfo customerInfo) {
		this.customerInfo = customerInfo;
	}

	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(DeviceInfo deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public String getMsgHash() {
		return msgHash;
	}

	public void setMsgHash(String msgHash) {
		this.msgHash = msgHash;
	}

	public CardInfo getCardInfo() {
		return cardInfo;
	}

	public void setCardInfo(CardInfo cardInfo) {
		this.cardInfo = cardInfo;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getMerchantReturnUrl() {
		return merchantReturnUrl;
	}

	public void setMerchantReturnUrl(String merchantReturnUrl) {
		this.merchantReturnUrl = merchantReturnUrl;
	}

	public boolean isBillingAddressRequired() {
		return billingAddressRequired;
	}

	public void setBillingAddressRequired(boolean billingAddressRequired) {
		this.billingAddressRequired = billingAddressRequired;
	}

	public boolean isShippingAddressRequired() {
		return shippingAddressRequired;
	}

	public void setShippingAddressRequired(boolean shippingAddressRequired) {
		this.shippingAddressRequired = shippingAddressRequired;
	}

	public boolean isProfileRequired() {
		return profileRequired;
	}

	public void setProfileRequired(boolean profileRequired) {
		this.profileRequired = profileRequired;
	}

	public boolean isPromptPromoCode() {
		return promptPromoCode;
	}

	public void setPromptPromoCode(boolean promptPromoCode) {
		this.promptPromoCode = promptPromoCode;
	}

	public CardFee getCardFee() {
		return cardFee;
	}

	public void setCardFee(CardFee cardFee) {
		this.cardFee = cardFee;
	}

}
