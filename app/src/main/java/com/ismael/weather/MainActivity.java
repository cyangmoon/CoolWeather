package com.ismael.weather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ismael.weather.gson.Weather;
import com.ismael.weather.util.InternetUtility;
import com.ismael.weather.util.MyToast;
import com.ismael.weather.util.WeatherUtility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private long lastRefreshTime = 0;
    public Weather weather = Weather.getInstance();
    @SuppressLint("StaticFieldLeak")
    public static Activity instance = null;
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        context = getApplicationContext();
        init();
    }

    public void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//Activity全屏显示，Activity顶端布局部分会被状态遮住。
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.bg);
        swipeRefreshLayout.setProgressViewEndTarget(false,getResources().getDisplayMetrics().heightPixels/6);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (InternetUtility.isNetworkConnected(MainActivity.instance)) {
                    if((findViewById(R.id.image)).getBackground()==null){
                        settingBingImage();
                    }
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    int countyCode = prefs.getInt("currentCountyCode", 0);
                    String language = prefs.getString("language","en");
                    if(countyCode != 0) {
                        WeatherUtility.refreshWeather("CN" + countyCode, language, new WeatherUtility.RefreshFinishedListener() {
                            @Override
                            public void onFinish() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                                        showWeatherData();
                                        showTimeInterval();
                                        swipeRefreshLayout.setRefreshing(false);
                                        MyToast.toastMessage(getApplicationContext(), "Weather updated", Toast.LENGTH_SHORT);
                                    }
                                });
                            }
                        });
                    }else {
                        WeatherUtility.refreshWeather(language, new WeatherUtility.RefreshFinishedListener() {
                            @Override
                            public void onFinish() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                                        editor.putInt("countyCode",Integer.parseInt(weather.weatherNow.basic.countyIdWithCn.substring(2)));
                                        editor.apply();
                                        showWeatherData();
                                        showTimeInterval();
                                        swipeRefreshLayout.setRefreshing(false);
                                        MyToast.toastMessage(getApplicationContext(), "Weather updated", Toast.LENGTH_SHORT);
                                    }
                                });
                            }
                        });
                    }
                    lastRefreshTime = System.currentTimeMillis();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            MyToast.toastMessage(getApplicationContext(), "Internet not available", Toast.LENGTH_SHORT);
                        }
                    });
                }
            }
        });
        updateWeatherDataFromServer();
        settingBingImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.selected_city:
                startActivity(new Intent(this,SelectedCityActivity.class));
                break;
            case R.id.settings:
                Toast.makeText(this, "You clicked Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.about:
                Toast.makeText(this, "You clicked About", Toast.LENGTH_SHORT).show();
            default:
        }
        return true;
    }

    public void settingBingImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL("http://guolin.tech/api/bing_pic").openConnection();
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    final StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }
                    URL myurl = new URL(result.toString());
                    HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
                    conn.setConnectTimeout(6000);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    final Bitmap bmp = BitmapFactory.decodeStream(is);
                    is.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.image_relativeLayout).setVisibility(View.VISIBLE);
                            Glide.with(getApplicationContext()).load(result.toString()).into((ImageView)findViewById(R.id.image));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    void updateWeatherDataFromServer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int countyCode = prefs.getInt("currentCountyCode", 0);
        String language = prefs.getString("language","en");
            if ((int) ((System.currentTimeMillis() - lastRefreshTime) / (1000 * 60)) >= 60) {
                if (InternetUtility.isNetworkConnected(MainActivity.instance)) {
                    if (countyCode != 0) {
                        WeatherUtility.refreshWeather("CN"+countyCode,language,new WeatherUtility.RefreshFinishedListener() {
                        @Override
                        public void onFinish() {
                            lastRefreshTime = System.currentTimeMillis();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showTimeInterval();
                                    showWeatherData();
                                }
                            });
                        }
                    });
                }else {
                        WeatherUtility.refreshWeather(language,new WeatherUtility.RefreshFinishedListener() {
                            @Override
                            public void onFinish() {
                                lastRefreshTime = System.currentTimeMillis();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int countyCode = Integer.parseInt(weather.weatherNow.basic.countyIdWithCn.substring(2));
                                        String countyName = weather.weatherNow.basic.location;
                                        SharedPreferences prfs =  PreferenceManager.getDefaultSharedPreferences(MainActivity.instance);
                                        SharedPreferences.Editor editor =prfs.edit();
                                        if(prfs.getString(countyName,"NotSavedCity").equals("NotSavedCity")){
                                            editor.putInt(countyName,countyCode);
                                        }
                                        editor.putInt("currentCountyCode",countyCode);
                                        editor.apply();
                                        showTimeInterval();
                                        showWeatherData();
                                    }
                                });
                            }
                        });
                    }
            } else {
                MyToast.toastMessage(getApplicationContext(), "Internet not available", Toast.LENGTH_SHORT);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    void showWeatherData(){
        ((TextView)findViewById(R.id.location_textView)).setText(weather.weatherNow.basic.location);
        ((TextView)findViewById(R.id.weather_now_tmp_textView)).setText(weather.weatherNow.now.tmp+" ℃");
        StringBuilder sb = new StringBuilder();
        sb.append(weather.weatherNow.now.cond_txt)
                .append(" ")
                .append(weather.dailyForecastList.dailyForcastList.get(0).tmp_max)
                .append("/")
                .append(weather.dailyForecastList.dailyForcastList.get(0).tmp_min)
                .append("°");
        ((TextView)findViewById(R.id.weather_now_type_textView)).setText(sb);
        //for(View view:(ViewGroup)findViewById(R.id.daily_forecast))
        //todo
    }

    @SuppressLint("SetTextI18n")
    void showTimeInterval(){
        long currentTime = System.currentTimeMillis();
        int refreshTimeInterval = (int)((currentTime-lastRefreshTime)/(1000*60));
        if(refreshTimeInterval==0){
            ((TextView)findViewById(R.id.refresh_textView)).setText("just updated");
        }else if((System.currentTimeMillis() - lastRefreshTime) / (1000 * 60)<60){
            ((TextView)findViewById(R.id.refresh_textView)).setText("updated "+refreshTimeInterval+" mins ago");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null){
            if (InternetUtility.isNetworkConnected(MainActivity.instance)) {
                if((findViewById(R.id.image)).getBackground()==null){
                    settingBingImage();
                }
                updateWeatherDataFromServer();
                findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                showTimeInterval();
            } else {
                findViewById(R.id.scrollView).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.location_textView)).setText(R.string.app_name);
                ((TextView)findViewById(R.id.refresh_textView)).setText(R.string.net_not_available);
            }
        }
        if ((int) ((System.currentTimeMillis() - lastRefreshTime) / (1000 * 60)) <= 60) {
            showTimeInterval();
        }else {
            if (InternetUtility.isNetworkConnected(MainActivity.instance)) {
                if((findViewById(R.id.image)).getBackground()==null){
                    settingBingImage();
                }
                updateWeatherDataFromServer();
                findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                showTimeInterval();
            } else {
                findViewById(R.id.scrollView).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.location_textView)).setText(R.string.app_name);
                ((TextView)findViewById(R.id.refresh_textView)).setText(R.string.net_not_available);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }
}
