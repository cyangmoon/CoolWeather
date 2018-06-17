package com.ismael.weather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {

    public String location;
    @SerializedName("parent_city")
    public String City;
    @SerializedName("admin_area")
    public String province;

}
