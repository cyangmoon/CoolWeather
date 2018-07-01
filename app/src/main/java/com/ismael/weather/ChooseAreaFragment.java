package com.ismael.weather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private List<County> countyList = new ArrayList<>();
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_area, container, false);
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
                    int countyCode = countyList.get(position).getCountyCode();
                    String countyName = countyList.get(position).getCountyName();
                    SharedPreferences prfs =  getActivity().getSharedPreferences("added_city", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor =prfs.edit();
                    if(prfs.getInt(countyName,0) == 0){
                        editor.putInt(countyName,countyCode);
                        editor.apply();
                    }
                    SharedPreferences.Editor editorForCurrentCounty= PreferenceManager.getDefaultSharedPreferences(MainActivity.instance).edit();
                    editorForCurrentCounty.putInt("currentCountyCode", countyCode);
                    editorForCurrentCounty.apply();
                    getActivity().setResult(Activity.RESULT_OK,null);
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

    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        parseXmlWithDom("province",null);
        if (provinceList.size()>0) {
            getActivity().findViewById(R.id.loading_progressBar).setVisibility(View.GONE);
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }
    }

    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        parseXmlWithDom("city",selectedProvince.getProvinceId());
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
        parseXmlWithDom("county",selectedCity.getCityId());
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

    private void parseXmlWithDom(final String type,String id){
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(getResources().getAssets().open("county_code_weather_china.xml"));
            switch (type) {
                case "province":
                    provinceList.clear();
                    NodeList nodeListOfProvince = document.getElementsByTagName("province");
                    Log.d("DOM", "Total provinces: " + nodeListOfProvince.getLength());
                    for (int i = 0; i < nodeListOfProvince.getLength(); i++) {
                        NamedNodeMap attr = nodeListOfProvince.item(i).getAttributes();
                        Province province = new Province();
                        province.setProvinceId(attr.item(0).getNodeValue());
                        province.setProvinceName(attr.item(1).getNodeValue());
                        provinceList.add(province);
                    }
                    break;
                case "city":
                    cityList.clear();
                    Element provinceNode = document.getElementById(id);
                    NodeList nodeListOfCity = provinceNode.getElementsByTagName("city");
                    for (int j = 0; j < nodeListOfCity.getLength(); j++) {
                        NamedNodeMap attr = nodeListOfCity.item(j).getAttributes();
                        City city = new City();
                        city.setCityId(attr.item(0).getNodeValue());
                        city.setCityName(attr.item(1).getNodeValue());
                        cityList.add(city);
                    }
                    break;
                default:
                    countyList.clear();
                    Element cityNode = document.getElementById(id);
                    NodeList nodeListOfCounty = cityNode.getElementsByTagName("county");
                    for (int m = 0; m < nodeListOfCounty.getLength(); m++) {
                        NamedNodeMap attr = nodeListOfCounty.item(m).getAttributes();
                        County county = new County();
                        county.setCityId(attr.item(0).getNodeValue());
                        county.setCountyName(attr.item(1).getNodeValue());
                        county.setCountyCode(Integer.parseInt(attr.item(2).getNodeValue()));
                        countyList.add(county);
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e("DOM",e.getMessage()+ Arrays.toString(e.getStackTrace()));
        }
    }

}
