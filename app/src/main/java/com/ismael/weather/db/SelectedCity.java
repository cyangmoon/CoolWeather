package com.ismael.weather.db;

public class SelectedCity {
    private String cityName="Loading...";
    private String cityWeatherInfo = "......";
    private int countyCode = 0;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityWeatherInfo() {
        return cityWeatherInfo;
    }

    public void setCityWeatherInfo(String cityWeatherInfo) {
        this.cityWeatherInfo = cityWeatherInfo;
    }

    public int getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(int countyCode) {
        this.countyCode = countyCode;
    }
}
