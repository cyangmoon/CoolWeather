package com.ismael.weather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DailyForecastList {

    @SerializedName("daily_forecast")
    public List<DailyForcast> dailyForcastList;

    public class DailyForcast {
        public String cond_txt_d;
        public String cond_txt_n;
        public String date;
        public String hum;
        public String mr;
        public String ms;
        public String sr;
        public String ss;
        public String tmp_max;
        public String tmp_min;
        public String wind_dir;
        public String wind_sc;
    }

}
