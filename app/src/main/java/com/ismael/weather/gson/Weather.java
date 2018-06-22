package com.ismael.weather.gson;

public class Weather {

    public WeatherNow weatherNow;
    public DailyForecastList daily;
    public HourlyForecastList hourly;

    private Weather(){}

    private static Weather weather = new Weather();

    public static Weather getInstance(){
        return weather;
    }

}
