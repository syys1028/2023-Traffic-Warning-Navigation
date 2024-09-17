package com.ubit.blackice;

public class openapiData {

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

    public void setIce_type(String type) {
        this.type = type;
    }
    public String getDetail_type() { return detail_type;}
    public void setDetaiil_type() { this.detail_type = detail_type;}

    //public String getMessage() { return message;}
    //public void setMessage(String message) { this.message = message;}

    private String datetime;
    private float latitude;
    private float longitude;
    private String type;
    private String detail_type;
    private String message;
}
