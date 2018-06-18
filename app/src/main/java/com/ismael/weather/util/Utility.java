package com.ismael.weather.util;

import android.util.Log;

import com.google.gson.Gson;
import com.ismael.weather.gson.DailyForecastList;
import com.ismael.weather.gson.HourlyForecastList;
import com.ismael.weather.gson.WeatherNow;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utility {

    public static WeatherNow handleWeatherNowResponse(URL url) {
        try {
            JSONObject jsonObject = new JSONObject(httpResponse(url));
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            WeatherNow weatherNow = new Gson().fromJson(weatherContent, WeatherNow.class);
            Log.i("weather ", weatherNow.toString());
            return weatherNow;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DailyForecastList handleDailyForecastResponse(URL url) {
        try {
            JSONObject jsonObject = new JSONObject(httpResponse(url));
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            DailyForecastList dailyForecastList = new Gson().fromJson(weatherContent, DailyForecastList.class);
            return dailyForecastList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HourlyForecastList handleHourlyForecastResponse(URL url) {
        try {
            JSONObject jsonObject = new JSONObject(httpResponse(url));
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            HourlyForecastList hourlyForecastList = new Gson().fromJson(weatherContent, HourlyForecastList.class);
            return hourlyForecastList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

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
        }
        finally {
            if(connection != null) connection.disconnect();
        }
        return null;
    }
}
