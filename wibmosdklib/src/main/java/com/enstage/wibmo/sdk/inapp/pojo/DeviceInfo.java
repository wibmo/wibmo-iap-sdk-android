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
 * //import org.codehaus.jackson.annotate.JsonIgnore;
 * //Use to ignore any method from populating in json @JsonIgnore
 * @author Preetham
 */

public class DeviceInfo implements Serializable {
    private static final long serialVersionUID = 1L;

	private String wibmoSdkVersion;
	private String wibmoAppVersion;
	private String deviceID;
	private String deviceMake;
	private String deviceModel;
	private String osType;
	private String osVersion;
    private boolean appInstalled;

    private int deviceIDType = 3;//mobile
    private int deviceType = 3;//mobile


    public String getWibmoSdkVersion() {
		return wibmoSdkVersion;
	}

	public void setWibmoSdkVersion(String wibmoSdkVersion) {
		this.wibmoSdkVersion = wibmoSdkVersion;
	}

	public String getWibmoAppVersion() {
		return wibmoAppVersion;
	}

	public void setWibmoAppVersion(String wibmoAppVersion) {
		this.wibmoAppVersion = wibmoAppVersion;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getDeviceMake() {
		return deviceMake;
	}

	public void setDeviceMake(String deviceMake) {
		this.deviceMake = deviceMake;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

    public boolean isAppInstalled() {
        return appInstalled;
    }

    public void setAppInstalled(boolean appInstalled) {
        this.appInstalled = appInstalled;
    }

    public int getDeviceIDType() {
        return deviceIDType;
    }

    public void setDeviceIDType(int deviceIDType) {
        this.deviceIDType = deviceIDType;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }
}
