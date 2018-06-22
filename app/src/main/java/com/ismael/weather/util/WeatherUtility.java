package com.ismael.weather.util;

import android.util.Log;

import com.google.gson.Gson;
import com.ismael.weather.gson.DailyForecastList;
import com.ismael.weather.gson.HourlyForecastList;
import com.ismael.weather.gson.Weather;
import com.ismael.weather.gson.WeatherNow;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WeatherUtility {

    public interface RefreshFinishedListener{ void onFinish();}

    private static String httpResponse(URL url) {
        HttpURLConnection connection = null;
        StringBuilder sb = new StringBuilder();
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(connection != null) connection.disconnect();
        }
        return null;
    }

    private static WeatherNow handleWeatherNowResponse(URL url) {
        try {
            JSONObject jsonObject = new JSONObject(httpResponse(url));
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, WeatherNow.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static DailyForecastList handleDailyForecastResponse(URL url) {
        try {
            JSONObject jsonObject = new JSONObject(httpResponse(url));
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, DailyForecastList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HourlyForecastList handleHourlyForecastResponse(URL url) {
        try {
            JSONObject jsonObject = new JSONObject(httpResponse(url));
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, HourlyForecastList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void refreshWeather(final String locationCode,final String language,final RefreshFinishedListener listener) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder paras = new StringBuilder();
                    paras.append("location=")
                            .append(locationCode)
                            .append("&key=2ddc493728214103a449996c292367ee&lang=")
                            .append(language);
                    try {
                        URL url_1 = new URL("https://free-api.heweather.com/s6/weather/now?" + paras);
                        Weather.getInstance().weatherNow = WeatherUtility.handleWeatherNowResponse(url_1);
                        URL url_3 = new URL("https://free-api.heweather.com/s6/weather/forecast?" + paras);
                        Weather.getInstance().daily = WeatherUtility.handleDailyForecastResponse(url_3);
                        URL url_2 = new URL("https://free-api.heweather.com/s6/weather/hourly?" + paras);
                        Weather.getInstance().hourly = WeatherUtility.handleHourlyForecastResponse(url_2);
                        Log.i("weather", "refreshed weather");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } finally {
                        listener.onFinish();
                    }
                }
            }).start();
    }

    public static void refreshWeather(final String language,final RefreshFinishedListener listener){
        refreshWeather("auto_ip",language,listener);
    }

}
