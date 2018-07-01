package com.ismael.weather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ismael.weather.custom.ReboundScrollView;
import com.ismael.weather.custom.TopWaveSurfaceView;
import com.ismael.weather.gson.Weather;
import com.ismael.weather.util.BasicTool;
import com.ismael.weather.util.InternetUtility;
import com.ismael.weather.util.MyToast;
import com.ismael.weather.util.ThreadFinishListener;
import com.ismael.weather.util.WeatherUtility;

import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static Activity instance = null;
    private long lastRefreshTime = 0;

    public Weather weather = Weather.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        init();
    }

    public void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//Activity全屏显示，Activity顶端布局部分会被状态遮住。
        getWindow().setStatusBarColor(getColor(R.color.purple));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentCountyCode",sharedPreferences.getInt("permanentCountyCode",0));
        editor.apply();
        ((TextView)findViewById(R.id.location_textView)).setText(getString(R.string.app_name));
        updateWeatherDataFromServer();
        ((ReboundScrollView)findViewById(R.id.scrollView)).setOnReboundEndListener(new MyOnReboundListener());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        RelativeLayout mrlay = findViewById(R.id.now_relativeLayout);
        android.view.ViewGroup.LayoutParams layoutParams =mrlay.getLayoutParams();
        layoutParams.height = displayMetrics.heightPixels-BasicTool.getActionBarHeight(this)-BasicTool.getStatusBarHeight(this);
        mrlay.setLayoutParams(layoutParams);
        ((TopWaveSurfaceView)findViewById(R.id.topWave)).startDrawingThread();
    }

    private  class  MyOnReboundListener implements ReboundScrollView.OnReboundEndListener{

        @Override
        public void onReboundTopComplete() {
            if (InternetUtility.isNetworkConnected(MainActivity.instance)) {
                SharedPreferences.Editor editor = MainActivity.instance.getSharedPreferences("weather_buffer",MODE_PRIVATE).edit();
                editor.remove("weather_now");
                editor.remove("weather_daily");
                editor.remove("weather_hourly");
                editor.apply();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                int countyCode = prefs.getInt("currentCountyCode", 0);
                String language = prefs.getString("language", "en");
                if (countyCode != 0) {
                    WeatherUtility.refreshWeather("CN" + countyCode, language, new ThreadFinishListener() {
                        @Override
                        public void onFinish() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showWeatherData();
                                    showTimeInterval();
                                    //  mSwipeRefreshLayout.setRefreshing(false);
                                    MyToast.toastMessage(getApplicationContext(), "Weather updated", Toast.LENGTH_SHORT,getColor(R.color.gray_white_some));
                                }
                            });
                        }
                    });
                } else {
                    WeatherUtility.refreshWeather(language, new ThreadFinishListener() {
                        @Override
                        public void onFinish() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int countyCode=Integer.parseInt(weather.weatherNow.basic.countyIdWithCn.substring(2));
                                    String countyName = weather.weatherNow.basic.location;
                                    SharedPreferences sharedPreferences = getSharedPreferences("added_city",MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    if (sharedPreferences.getString(countyName, "NotSavedCity").equals("NotSavedCity")) { editor.putInt(countyName, countyCode); }
                                    editor.apply();
                                    //保存当前城市编码
                                    SharedPreferences.Editor editorForCurrent = PreferenceManager.getDefaultSharedPreferences(MainActivity.instance).edit();
                                    editorForCurrent.putInt("currentCountyCode", countyCode);
                                    editorForCurrent.putInt("permanentCountyCode",countyCode);
                                    editorForCurrent.apply();
                                    showWeatherData();
                                    showTimeInterval();
                                    //   mSwipeRefreshLayout.setRefreshing(false);
                                    MyToast.toastMessage(getApplicationContext(), "Weather updated", Toast.LENGTH_SHORT,getColor(R.color.gray_white_some));
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
                        ((TextView)findViewById(R.id.time_interval_textView)).setText("Internet not available");
                        MyToast.toastMessage(getApplicationContext(), "Internet not available", Toast.LENGTH_SHORT,getColor(R.color.gray_white_some));
                    }
                });
            }
        }

        @Override
        public void onReboundBottomComplete() {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showTimeInterval();
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
        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK) {
                    ScrollView scrollView= findViewById(R.id.scrollView);
                    scrollView.fullScroll(View.FOCUS_UP);
                    if (InternetUtility.isNetworkConnected(MainActivity.instance)) {
                        updateWeatherDataFromServer();
                        showTimeInterval();
                    } else {
                        scrollView.setVisibility(View.INVISIBLE);
                        ((TextView) findViewById(R.id.location_textView)).setText(R.string.app_name);
                        ((TextView) findViewById(R.id.time_interval_textView)).setText(R.string.net_not_available);
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
                WeatherUtility.refreshWeather("CN" + countyCode, language, new ThreadFinishListener() {
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
                WeatherUtility.refreshWeather(language, new ThreadFinishListener() {
                    @Override
                    public void onFinish() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int countyCode = Integer.parseInt(weather.weatherNow.basic.countyIdWithCn.substring(2));
                                String countyName = weather.weatherNow.basic.location;
                                SharedPreferences sharedPreferences = getSharedPreferences("added_city",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                if (sharedPreferences.getString(countyName, "NotSavedCity").equals("NotSavedCity")) {
                                    editor.putInt(countyName, countyCode);
                                    editor.apply();
                                }
                                SharedPreferences.Editor editorForCurrentCounty= PreferenceManager.getDefaultSharedPreferences(MainActivity.instance).edit();
                                editorForCurrentCounty.putInt("currentCountyCode", countyCode);
                                editorForCurrentCounty.putInt("permanentCountyCode",countyCode);
                                editorForCurrentCounty.apply();
                                showTimeInterval();
                                showWeatherData();
                            }
                        });
                    }
                });
            }
            lastRefreshTime = System.currentTimeMillis();

        } else {
            findViewById(R.id.scrollView).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.location_textView)).setText(R.string.app_name);
            ((TextView) findViewById(R.id.time_interval_textView)).setText(R.string.net_not_available);
            MyToast.toastMessage(getApplicationContext(), "Internet not available", Toast.LENGTH_SHORT,getColor(R.color.white));
        }
    }

    void showWeatherData() {
        findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
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
        LinearLayout now_linearLayout = findViewById(R.id.hourly_forecast_linearLayout);
        for (int i = 0; i < now_linearLayout.getChildCount(); i++) {
            View view = now_linearLayout.getChildAt(i);
            if (view instanceof LinearLayout) {
                String time = weather.hourly.hourlyForecastList.get(i).time;

                ((TextView) ((LinearLayout) view).getChildAt(0)).setText(time.substring(time.length() - 5));
                ((TextView) ((LinearLayout) view).getChildAt(2)).setText(weather.hourly.hourlyForecastList.get(i).tmp + "°");
            }
        }
        LinearLayout dailyLinearLayout = findViewById(R.id.daily_linearLayout);
        Calendar calendar = Calendar.getInstance();
        ((TextView)((LinearLayout)dailyLinearLayout.getChildAt(0)).getChildAt(0)).setText(calendar.get(Calendar.MONTH)+1+"/"+calendar.get(Calendar.DATE));
        ((TextView)((LinearLayout)dailyLinearLayout.getChildAt(0)).getChildAt(1)).setText("Today");
        for(int j=1;j<dailyLinearLayout.getChildCount();j++){
            calendar.add(Calendar.DATE,1);
            ((TextView)((LinearLayout)dailyLinearLayout.getChildAt(j)).getChildAt(0)).setText(calendar.get(Calendar.MONTH)+1+"/"+calendar.get(Calendar.DATE));
            ((TextView)((LinearLayout)dailyLinearLayout.getChildAt(j)).getChildAt(1)).setText(calendar.getTime().toString().substring(0,3));
        }

    }

    void showTimeInterval() {
        if(InternetUtility.isNetworkConnected(this)) {
            long currentTime = System.currentTimeMillis();
            int refreshTimeInterval = (int) ((currentTime - lastRefreshTime) / (1000 * 60));
            if (refreshTimeInterval == 0) {
                ((TextView) findViewById(R.id.time_interval_textView)).setText(R.string.just_now);
            } else if (refreshTimeInterval < 60) {
                ((TextView) findViewById(R.id.time_interval_textView)).setText("updated " + refreshTimeInterval + " mins ago");
            } else if (refreshTimeInterval < 120) {
                ((TextView) findViewById(R.id.time_interval_textView)).setText(R.string.updated_1_hour_ago);
            } else {
                ((TextView) findViewById(R.id.time_interval_textView)).setText("updated " + refreshTimeInterval / 60 + " hours ago");
            }
        }
    }
}
