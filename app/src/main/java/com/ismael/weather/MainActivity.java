package com.ismael.weather;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ismael.weather.gson.Weather;
import com.ismael.weather.util.BasicTool;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        init();
    }

    public void init() {
        //标题栏质感化
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //状态栏沉浸
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//Activity全屏显示，Activity顶端布局部分会被状态遮住。
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        //下拉刷新设置
        initSwipeRefreshLayout();
        updateWeatherDataFromServer();
        getBingImage();
    }

    private void initSwipeRefreshLayout(){
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setProgressViewEndTarget(true, getResources().getDisplayMetrics().heightPixels /6);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (InternetUtility.isNetworkConnected(MainActivity.instance)) {
                    if ((findViewById(R.id.image)).getBackground() == null) { getBingImage();}
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    int countyCode = prefs.getInt("currentCountyCode", 0);
                    String language = prefs.getString("language", "en");
                    if (countyCode != 0) {
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
                    } else {
                        WeatherUtility.refreshWeather(language, new WeatherUtility.RefreshFinishedListener() {
                            @Override
                            public void onFinish() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                                        editor.putInt("countyCode", Integer.parseInt(weather.weatherNow.basic.countyIdWithCn.substring(2)));
                                        editor.apply();
                                        lastRefreshTime = System.currentTimeMillis();
                                        showWeatherData();
                                        showTimeInterval();
                                        swipeRefreshLayout.setRefreshing(false);
                                        MyToast.toastMessage(getApplicationContext(), "Weather updated", Toast.LENGTH_SHORT);
                                    }
                                });
                            }
                        });
                    }
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
    }


    boolean firstBoot = true;
    @Override
    protected void onResume() {
        super.onResume();
        if(firstBoot){
            firstBoot = false;
        }else {
            showTimeInterval();
        }
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
                startActivityForResult(new Intent(this, SelectedCityActivity.class), 1);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("order","1--onActivityResult");

        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK) {
                    if (InternetUtility.isNetworkConnected(MainActivity.instance)) {
                        if ((findViewById(R.id.image)).getBackground() == null) {
                            getBingImage();
                        }
                        updateWeatherDataFromServer();
                        findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                        showTimeInterval();
                    } else {
                        findViewById(R.id.scrollView).setVisibility(View.INVISIBLE);
                        ((TextView) findViewById(R.id.location_textView)).setText(R.string.app_name);
                        ((TextView) findViewById(R.id.refresh_textView)).setText(R.string.net_not_available);
                    }
                }
            }
        }
    }

    void updateWeatherDataFromServer() {
        if (InternetUtility.isNetworkConnected(MainActivity.instance)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int countyCode = prefs.getInt("currentCountyCode", 0);
            String language = prefs.getString("language", "en");
            if (countyCode != 0) {
                WeatherUtility.refreshWeather("CN" + countyCode, language, new WeatherUtility.RefreshFinishedListener() {
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
            } else {
                WeatherUtility.refreshWeather(language, new WeatherUtility.RefreshFinishedListener() {
                    @Override
                    public void onFinish() {
                        lastRefreshTime = System.currentTimeMillis();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int countyCode = Integer.parseInt(weather.weatherNow.basic.countyIdWithCn.substring(2));
                                String countyName = weather.weatherNow.basic.location;
                                SharedPreferences prfs = PreferenceManager.getDefaultSharedPreferences(MainActivity.instance);
                                SharedPreferences.Editor editor = prfs.edit();
                                if (prfs.getString(countyName, "NotSavedCity").equals("NotSavedCity")) { editor.putInt(countyName, countyCode); }
                                editor.putInt("currentCountyCode", countyCode);
                                editor.apply();
                                showTimeInterval();
                                showWeatherData();
                            }
                        });
                    }
                });
            }
        } else {
            findViewById(R.id.scrollView).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.location_textView)).setText(R.string.app_name);
            ((TextView) findViewById(R.id.refresh_textView)).setText(R.string.net_not_available);
            MyToast.toastMessage(getApplicationContext(), "Internet not available", Toast.LENGTH_SHORT);
        }
    }

    public void getBingImage() {
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
                            Glide.with(getApplicationContext()).load(result.toString()).into((ImageView) findViewById(R.id.image));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    void showWeatherData() {
        String location = BasicTool.firstUpperCaseString(weather.weatherNow.basic.location);
        ((TextView) findViewById(R.id.location_textView)).setText(location);
        ((TextView) findViewById(R.id.weather_now_tmp_textView)).setText(weather.weatherNow.now.tmp +"°");
        StringBuilder sb = new StringBuilder();
        sb.append(weather.weatherNow.now.cond_txt)
                .append("  ")
                .append(weather.daily.dailyForecastList.get(0).tmp_max)
                .append("/")
                .append(weather.daily.dailyForecastList.get(0).tmp_min)
                .append("℃");
        ((TextView) findViewById(R.id.weather_now_type_textView)).setText(sb);
        LinearLayout linearLayout = findViewById(R.id.hourly_forecast_linearLayout);
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            if (view instanceof LinearLayout) {
                String time = weather.hourly.hourlyForecastList.get(i).time;

                ((TextView) ((LinearLayout) view).getChildAt(0)).setText(time.substring(time.length() - 5));
                ((TextView) ((LinearLayout) view).getChildAt(2)).setText(weather.hourly.hourlyForecastList.get(i).tmp + "°");
            }
        }
    }

    void showTimeInterval() {
        if(InternetUtility.isNetworkConnected(this)) {

            long currentTime = System.currentTimeMillis();
            int refreshTimeInterval = (int) ((currentTime - lastRefreshTime) / (1000 * 60));
            if (refreshTimeInterval == 0) {
                ((TextView) findViewById(R.id.refresh_textView)).setText(R.string.just_now);
            } else if (refreshTimeInterval < 60) {
                ((TextView) findViewById(R.id.refresh_textView)).setText("updated " + refreshTimeInterval + " mins ago");
            } else if (refreshTimeInterval < 120) {
                ((TextView) findViewById(R.id.refresh_textView)).setText(R.string.updated_1_hour_ago);
            } else {
                ((TextView) findViewById(R.id.refresh_textView)).setText("updated " + refreshTimeInterval / 60 + " hours ago");
            }
        }
    }
}
