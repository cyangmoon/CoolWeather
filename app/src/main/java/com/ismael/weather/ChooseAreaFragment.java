package com.ismael.weather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ismael.weather.db.City;
import com.ismael.weather.db.County;
import com.ismael.weather.db.Province;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText =  view.findViewById(R.id.title_text);
        backButton =  view.findViewById(R.id.back_button);
        listView =  view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queryProvinces();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY) {
                    int getCountyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", getCountyCode);
                    startActivity(intent);
                    Objects.requireNonNull(getActivity()).finish();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
    }

    interface NetListener{
        void finishLoad();
        void startLoad();
    }
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size()>0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            DataSupport.deleteAll(Province.class,"");
            DataSupport.deleteAll(County.class,"");
            DataSupport.deleteAll(City.class,"");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    queryFromServer("https://www.cnblogs.com/oucbl/p/6138963.html",new NetListener(){
                        @Override
                        public void startLoad() {
                            ((Activity)getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgressDialog();
                                }
                            });
                        }
                        @Override
                        public void finishLoad() {
                            ((Activity) Objects.requireNonNull(getContext())).runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    closeProgressDialog();
                                    queryProvinces();
                                }
                            });
                        }
                    });
                }
            }).start();
        }
    }

    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getProvinceId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
    }

    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getCityId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }
    }

    private void queryFromServer(String address,NetListener netListener) {
        netListener.startLoad();
        try {
            Document document = Jsoup.connect(address).get();
            Element body = document.getElementsByClass("preview").first();
            Elements provinces = body.getElementsByTag("h3");
            Province province;
            for(int i=1;i< provinces.size();i++){
                province = new Province();
                province.setProvinceName(provinces.get(i).text());
                province.setProvinceId(i);
                province.save();
            }
            Elements citysAndCountys = body.getElementsByClass("xiaoshujiang_code_container");
            int i=1;
            for(i=1;i<citysAndCountys.size();i++){
                City city;
                County county;
                String s = citysAndCountys.get(i).getElementsByTag("code").text().replaceAll(" ","");
                String[] cityOrCounty = s.split("\\n");
                int orderCityId = 0;
                for(int j = 0;j<cityOrCounty.length;j++){
                    if(!cityOrCounty[j].substring(cityOrCounty[j].length()-1).matches("\\d")){
                        orderCityId =1000*i+j ;
                        city = new City();
                        city.setProvinceId(i);
                        city.setCityName(cityOrCounty[j]);
                        city.setCityId(orderCityId);
                        city.save();
                    }else {
                        county = new County();
                        county.setCityId(orderCityId);
                        int m;
                        for(m=0;m<cityOrCounty[j].length();m++){
                            if(cityOrCounty[j].charAt(m)<58 && cityOrCounty[j].charAt(m)>47) break;
                        }
                        county.setCountyCode(Integer.parseInt(cityOrCounty[j].substring(m)));
                        county.setCountyName(cityOrCounty[j].substring(0,m));
                        county.save();
                    }
                }
            }
            netListener.finishLoad();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog((Activity)getContext());
            progressDialog.setMessage("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
