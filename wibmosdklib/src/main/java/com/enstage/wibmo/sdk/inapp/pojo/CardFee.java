package com.enstage.wibmo.sdk.inapp.pojo;

import java.io.Serializable;

/**
 * Created by sujay.abraham on 24/04/18.
 */

public class CardFee implements Serializable {
    private static final long serialVersionUID = 1L;

    private String creditCard;
    private String debitCard;
    private String prepaidCard;
    private String general;

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    public String getDebitCard() {
        return debitCard;
    }

    public void setDebitCard(String debitCard) {
        this.debitCard = debitCard;
    }

    public String getPrepaidCard() {
        return prepaidCard;
    }

    public void setPrepaidCard(String prepaidCard) {
        this.prepaidCard = prepaidCard;
    }

    public String getGeneral() {
        return general;
    }

    public void setGeneral(String general) {
        this.general = general;
    }
}
