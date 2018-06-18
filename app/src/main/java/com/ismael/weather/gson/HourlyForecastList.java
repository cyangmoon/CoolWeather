package com.ismael.weather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HourlyForecastList {

    @SerializedName("hourly")
    public List<HourlyForecast> hourlyForecastList;

    class HourlyForecast {
        public String cond_txt;
        public String hum;
        public String time;
        public String wind_dir;
        public String wind_sc;
        public String tmp;
    }

}
