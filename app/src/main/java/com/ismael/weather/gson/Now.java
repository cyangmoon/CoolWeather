package com.ismael.weather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("cond_text")
    public String type;
    public String windDir;
    public String l;


}
