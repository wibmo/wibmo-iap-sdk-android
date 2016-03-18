package com.enstage.wibmo.sdk.inapp.pojo;

/**
 * Created by Akshathkumar Shetty on 15/02/16.
 */
public class IAPaymentStatusResponse extends W2faResponse {
    private boolean authenticationSuccessful;
    private boolean chargeSuccessful;
    private boolean chargeAttempted;
    private String txnStatusCode;
    private String txnStatusDesc;

    public boolean isAuthenticationSuccessful() {
        return authenticationSuccessful;
    }

    public void setAuthenticationSuccessful(boolean authenticationSuccessful) {
        this.authenticationSuccessful = authenticationSuccessful;
    }

    public boolean isChargeSuccessful() {
        return chargeSuccessful;
    }

    public void setChargeSuccessful(boolean chargeSuccessful) {
        this.chargeSuccessful = chargeSuccessful;
    }

    public boolean isChargeAttempted() {
        return chargeAttempted;
    }

    public void setChargeAttempted(boolean chargeAttempted) {
        this.chargeAttempted = chargeAttempted;
    }

    public String getTxnStatusCode() {
        return txnStatusCode;
    }

    public void setTxnStatusCode(String txnStatusCode) {
        this.txnStatusCode = txnStatusCode;
    }

    public String getTxnStatusDesc() {
        return txnStatusDesc;
    }

    public void setTxnStatusDesc(String txnStatusDesc) {
        this.txnStatusDesc = txnStatusDesc;
    }
}
