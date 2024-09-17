package com.ubit.blackice;

public class blackiceData {
    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getIce_type() {
        return type;
    }

    public void setIce_type(String ice_type) {
        this.type = type;
    }

    private String datetime;
    private float latitude;
    private float longitude;
    private String type;
}
