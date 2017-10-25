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
    private String cardTokenRefNo;
    private boolean isPassCardS2S;

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

    public String getCardTokenRefNo() {
        return cardTokenRefNo;
    }

    public void setCardTokenRefNo(String cardTokenRefNo) {
        this.cardTokenRefNo = cardTokenRefNo;
    }

    public boolean isPassCardS2S() {
        return isPassCardS2S;
    }

    public void setPassCardS2S(boolean passCardS2S) {
        isPassCardS2S = passCardS2S;
    }
}
