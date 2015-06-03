package com.enstage.wibmo.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ashetty on 13/03/15.
 */
public class PhoneInfoBase {
    public static final String DEVICE_ID_TYPE = "3";

    private static MessageDigest md = null;

    static {
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error1: " + e);
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException er) {
                System.err.println("Error2: "+er);
            }
        }
    }

    private String deviceID;
    private String netCountryIsoCode;
    private String netOperator;
    private String netSubId;
    private boolean netRoaming;
    private String simSrNum;
    private String androidVersion;
    private String phoneMaker;
    private String phoneModel;

    private float gpsAccuracy;
    private double gpsLatitude;
    private double gpsLongitude;
    private long gpsTime;

    public static String mask(String data) {
        if(data==null) {
            return null;
        }
        int len = data.length();
        if(len<4 || md==null) {
            return data;
        }

        try {
            return new String(
                    Base64.encode(md.digest(data.getBytes("utf-8"))) ) +
                    ":"+ data.substring(len-4);
        } catch (UnsupportedEncodingException e) {
            return data;
        }
    }

    public String getDeviceName() {
        return "WibmoApp @ "+ getPhoneMaker() + " " +
                getPhoneModel() +  " - " + getNetOperatorName();
    }


    /**
     * @return the deviceID
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * @param deviceID the deviceID to set
     */
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    /**
     * @return the netCountryIsoCode
     */
    public String getNetCountryIsoCode() {
        return netCountryIsoCode;
    }

    /**
     * @param netCountryIsoCode the netCountryIsoCode to set
     */
    public void setNetCountryIsoCode(String netCountryIsoCode) {
        this.netCountryIsoCode = netCountryIsoCode;
    }

    /**
     * @return the netOperator
     */
    public String getNetOperator() {
        return netOperator;
    }

    public String getNetOperatorName() {
        if(getNetOperator()!=null) {
            int i = getNetOperator().indexOf("/");
            if(i!=-1) {
                return getNetOperator().substring(i+1);
            } else {
                return getNetOperator();
            }
        } else {
            return null;
        }
    }

    /**
     * @param netOperator the netOperator to set
     */
    public void setNetOperator(String netOperator) {
        this.netOperator = netOperator;
    }

    /**
     * @return the netSubId
     */
    public String getNetSubId() {
        return netSubId;
    }

    /**
     * @param netSubId the netSubId to set
     */
    public void setNetSubId(String netSubId) {
        this.netSubId = netSubId;
    }

    /**
     * @return the netRoaming
     */
    public boolean isNetRoaming() {
        return netRoaming;
    }

    /**
     * @param netRoaming the netRoaming to set
     */
    public void setNetRoaming(boolean netRoaming) {
        this.netRoaming = netRoaming;
    }

    /**
     * @return the simSrNum
     */
    public String getSimSrNum() {
        return simSrNum;
    }

    /**
     * @param simSrNum the simSrNum to set
     */
    public void setSimSrNum(String simSrNum) {
        this.simSrNum = simSrNum;
    }

    /**
     * @return the androidVersion
     */
    public String getAndroidVersion() {
        return androidVersion;
    }

    /**
     * @param androidVersion the androidVersion to set
     */
    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    /**
     * @return the phoneMaker
     */
    public String getPhoneMaker() {
        return phoneMaker;
    }

    /**
     * @param phoneMaker the phoneMaker to set
     */
    public void setPhoneMaker(String phoneMaker) {
        this.phoneMaker = phoneMaker;
    }

    /**
     * @return the phoneModel
     */
    public String getPhoneModel() {
        return phoneModel;
    }

    /**
     * @param phoneModel the phoneModel to set
     */
    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    /**
     * @return the gpsAccuracy
     */
    public float getGpsAccuracy() {
        return gpsAccuracy;
    }

    /**
     * @param gpsAccuracy the gpsAccuracy to set
     */
    public void setGpsAccuracy(float gpsAccuracy) {
        this.gpsAccuracy = gpsAccuracy;
    }

    /**
     * @return the gpsLatitude
     */
    public double getGpsLatitude() {
        return gpsLatitude;
    }

    /**
     * @param gpsLatitude the gpsLatitude to set
     */
    public void setGpsLatitude(double gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    /**
     * @return the gpsLongitude
     */
    public double getGpsLongitude() {
        return gpsLongitude;
    }

    /**
     * @param gpsLongitude the gpsLongitude to set
     */
    public void setGpsLongitude(double gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    /**
     * @return the gpsTime
     */
    public long getGpsTime() {
        return gpsTime;
    }

    /**
     * @param gpsTime the gpsTime to set
     */
    public void setGpsTime(long gpsTime) {
        this.gpsTime = gpsTime;
    }
}
