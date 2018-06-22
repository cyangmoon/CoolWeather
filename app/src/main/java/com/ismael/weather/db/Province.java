package com.ismael.weather.db;

import org.litepal.crud.DataSupport;

public class Province extends DataSupport {

    private String provinceName;
    private String provinceId;

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }
}
