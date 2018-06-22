package com.ismael.weather;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.Objects;

public class SelectedCityActivity extends AppCompatActivity implements View.OnClickListener {

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                this.finish();
                break;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab: {
                startActivityForResult(new Intent(this, ChooseAreaActivity.class),2);
                break;
            }
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("order","2--onActivityResult");
        switch (requestCode){
            case 2: {
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, null);
                    this.finish();
                }
            }
            default:
        }
    }
}
