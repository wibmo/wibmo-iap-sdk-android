package com.enstage.wibmo.sdk.inapp.pojo;

import java.io.Serializable;

/**
 *
 * @author Preetham Hegde
 */
public class CardInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cardId;
    private String cardnumber;
    private String cvv2;
    private String expiryMM;
    private String expiryYYYY;
    private String cardType;
    private String nameOnCard;

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public String getCvv2() {
        return cvv2;
    }

    public void setCvv2(String cvv2) {
        this.cvv2 = cvv2;
    }

    public String getExpiryMM() {
        return expiryMM;
    }

    public void setExpiryMM(String expiryMM) {
        this.expiryMM = expiryMM;
    }

    public String getExpiryYYYY() {
        return expiryYYYY;
    }

    public void setExpiryYYYY(String expiryYYYY) {
        this.expiryYYYY = expiryYYYY;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public void setNameOnCard(String nameOnCard) {
        this.nameOnCard = nameOnCard;
    }

}
