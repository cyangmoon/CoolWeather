package com.ismael.weather.gson;

import com.google.gson.annotations.SerializedName;

public class Info {

    @SerializedName("cid")
    public String countyIdWithCn;
    public String location;
    @SerializedName("parent_city")
    public String city;
    @SerializedName("admin_area")
    public String province;

}
