package com.ismael.weather.gson;

public class Weather {

    public WeatherNow weatherNow;
    public DailyForecastList dailyForecastList;
    public HourlyForecastList hourlyForecastList;

    private Weather(){}

    private static Weather weather = new Weather();

    public static Weather getInstance(){
        return weather;
    }

}
