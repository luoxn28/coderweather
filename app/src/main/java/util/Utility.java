package util;

import android.text.TextUtils;
import android.util.Log;

import model.City;
import model.CoderWeatherDB;
import model.County;
import model.Province;

/**
 * Created by luoxn28 on 2015/11/29.
 */
public class Utility {
    // 解析和处理服务器返回的省级数据，数据格式为 01|北京,02|上海,...
    public synchronized static boolean handleProvincesResponse(CoderWeatherDB coderWeatherDB, String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] allProvinces = response.split(",");
            if (allProvinces != null && allProvinces.length > 0) {
                for (String p : allProvinces) {
                    String[] array = p.split("\\|");
                    Province province = new Province();

                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    // 将解析出来的数据存储到Province表
                    coderWeatherDB.saveProvince(province);
                }
                return true;
            }
        }

        return false;
    }

    // 解析和处理服务器返回的市级数据，数据格式为 2101|杭州,2102|湖州,...
    public static boolean handleCitiesResponse(CoderWeatherDB coderWeatherDB,
                                               String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");
                    City city = new City();

                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    // 强解析出来的数据存储到City表
                    coderWeatherDB.saveCity(city);
                }
                return true;
            }
        }

        return false;
    }

    // 解析和处理服务器返回的县级数据，数据格式为 210101|杭州,210102|萧山,...
    public static boolean handleCountiesResponse(CoderWeatherDB coderWeatherDB,
                                                 String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0) {
                for (String c : allCounties) {
                    String[] array = c.split("\\|");
                    County county = new County();

                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    // 将解析出来的数据存储到County表
                    Log.d("hdu Utility", array[0] + array[1]);
                    coderWeatherDB.saveCounty(county);
                }
                return true;
            }
        }

        return false;
    }
}
