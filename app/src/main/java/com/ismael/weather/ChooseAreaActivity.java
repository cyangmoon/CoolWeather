package com.ismael.weather;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class ChooseAreaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("order","3--onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);
    }
}
