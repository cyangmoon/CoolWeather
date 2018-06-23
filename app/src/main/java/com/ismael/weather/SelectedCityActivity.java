package com.ismael.weather;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.ismael.weather.custom.SelectedCityAdapter;
import com.ismael.weather.db.SelectedCity;
import com.ismael.weather.gson.WeatherNow;
import com.ismael.weather.util.BasicTool;
import com.ismael.weather.util.InternetUtility;
import com.ismael.weather.util.ThreadFinishListener;
import com.ismael.weather.util.WeatherUtility;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SelectedCityActivity extends AppCompatActivity implements View.OnClickListener {

    private List<SelectedCity> selectedCityList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_city);
        Toolbar toolbar = findViewById(R.id.selected_city_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);//不显示标题
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);
        findViewById(R.id.fab).setOnClickListener(this);//设置悬浮按钮监听器
        initSelectedCityList("en",listener);
        RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        if(selectedCityList.size() > 5){
          //  ((FloatingActionButton)findViewById(R.id.fab)).setBackgroundTintList(getColor(R.color.white));
        }
    }

    private SelectedCityAdapter adapter = new SelectedCityAdapter(selectedCityList, this);
    private ThreadFinishListener listener = new ThreadFinishListener() {
        @Override
        public void onFinish() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                this.finish();
                break;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab: {
                startActivityForResult(new Intent(this, ChooseAreaActivity.class), 2);
                break;
            }
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("order", "2--onActivityResult");
        switch (requestCode) {
            case 2: {
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, null);
                    this.finish();
                }
            }
            default:
        }
    }

    private void initSelectedCityList(final String language,final ThreadFinishListener listener) {
        final Map<String, ?> allContent = getSharedPreferences("added_city", MODE_PRIVATE).getAll();
        for (int i =0;i<allContent.entrySet().size();i++){
            selectedCityList.add(new SelectedCity());
        }
        if(InternetUtility.isNetworkConnected(this)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int m = 0;
                    for (Map.Entry<String, ?> entry : allContent.entrySet()) {
                        StringBuilder paras = new StringBuilder();
                        paras.append("location=")
                                .append("CN"+entry.getValue().toString())
                                .append("&key=2ddc493728214103a449996c292367ee&lang=")
                                .append(language);
                        URL url = null;
                        try {
                            url = new URL("https://free-api.heweather.com/s6/weather/now?" + paras);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        WeatherNow weatherNow = WeatherUtility.handleWeatherNowResponse(url);
                        SelectedCity selectedCity = new SelectedCity();
                        selectedCity.setCountyCode(Integer.parseInt(entry.getValue().toString()));
                        selectedCity.setCityName(BasicTool.firstUpperCaseString(weatherNow.basic.location));
                        selectedCity.setCityWeatherInfo(weatherNow.now.tmp + "° " + weatherNow.now.cond_txt);
                        selectedCityList.set(m,selectedCity);
                        m++;
                        listener.onFinish();
                    }
                }
            }).start();
        }
    }
}
