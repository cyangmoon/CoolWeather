package com.ismael.weather.db;

import org.litepal.crud.DataSupport;

public class Province extends DataSupport {

    private String provinceName;
    private int provinceId;

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
