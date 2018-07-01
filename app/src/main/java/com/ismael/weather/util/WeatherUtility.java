package com.ismael.weather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.ismael.weather.MainActivity;
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

    public static String httpResponse(URL url) {
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
        } finally {
            if (connection != null) connection.disconnect();
        }
        return null;
    }

    public static WeatherNow handleWeatherNowResponse(URL url) {
        SharedPreferences sharedPreferences = MainActivity.instance.getSharedPreferences("weather_buffer", Context.MODE_PRIVATE);
        String weatherContent = sharedPreferences.getString("weather_now", null);
        try {
            if (weatherContent == null) {
                JSONObject jsonObject = new JSONObject(httpResponse(url));
                JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
                weatherContent = jsonArray.getJSONObject(0).toString();
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("weather_now", weatherContent);
            editor.apply();
            return new Gson().fromJson(weatherContent, WeatherNow.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DailyForecastList handleDailyForecastResponse(URL url) {
        SharedPreferences sharedPreferences = MainActivity.instance.getSharedPreferences("weather_buffer", Context.MODE_PRIVATE);
        String weatherContent = sharedPreferences.getString("weather_daily", null);
        try {
            if (weatherContent == null) {
                JSONObject jsonObject = new JSONObject(httpResponse(url));
                JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
                weatherContent = jsonArray.getJSONObject(0).toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("weather_daily", weatherContent);
                editor.apply();
            }
            return new Gson().fromJson(weatherContent, DailyForecastList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HourlyForecastList handleHourlyForecastResponse(URL url) {
        SharedPreferences sharedPreferences = MainActivity.instance.getSharedPreferences("weather_buffer", Context.MODE_PRIVATE);
        String weatherContent = sharedPreferences.getString("weather_hourly", null);
        try {
            if (weatherContent == null) {
            JSONObject jsonObject = new JSONObject(httpResponse(url));
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            weatherContent = jsonArray.getJSONObject(0).toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("weather_hourly", weatherContent);
                editor.apply();
            }
            return new Gson().fromJson(weatherContent, HourlyForecastList.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void refreshWeather(final String locationCode, final String language, final ThreadFinishListener listener) {
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

    public static void refreshWeather(final String language, final ThreadFinishListener listener) {
        refreshWeather("auto_ip", language, listener);
    }

}
