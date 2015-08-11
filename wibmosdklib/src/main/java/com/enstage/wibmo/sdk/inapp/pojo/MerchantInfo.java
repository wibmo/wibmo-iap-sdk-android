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
public class MerchantInfo  implements Serializable {
    private static final long serialVersionUID = 1L;

    private String merName;
	private String merId;
	private String merCountryCode;
	private String merAppId;


	public String getMerAppId() {
		return merAppId;
	}

	public void setMerAppId(String merAppId) {
		this.merAppId = merAppId;
	}



	public String getMerId() {
		return merId;
	}

	public void setMerId(String merId) {
		this.merId = merId;
	}

	public String getMerCountryCode() {
		return merCountryCode;
	}

	public void setMerCountryCode(String merCountryCode) {
		this.merCountryCode = merCountryCode;
	}

    public String getMerName() {
        return merName;
    }

    public void setMerName(String merName) {
        this.merName = merName;
    }
}
