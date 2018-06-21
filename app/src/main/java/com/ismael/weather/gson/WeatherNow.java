package com.ismael.weather.gson;

public class WeatherNow {
    public Info basic;
    public Now now;

    public String toString(){
        return basic.location+" "+basic.city+" "+basic.province +" :  "+now.cond_txt;
    }
}
