package com.ismael.weather;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ismael.weather.util.Utility;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String paras ="location=CN101201002&key=2ddc493728214103a449996c292367ee&lang=en";
                try {
                    URL url_1 = new URL("https://free-api.heweather.com/s6/weather/now?"+paras);
                    Utility.handleWeatherNowResponse(url_1);
                    URL url_3 = new URL("https://free-api.heweather.com/s6/weather/forecast?"+paras);
                    Utility.handleDailyForecastResponse(url_3);
                    URL url_2 = new URL("https://free-api.heweather.com/s6/weather/hourly?"+paras);
                    Utility.handleHourlyForecastResponse(url_2);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
