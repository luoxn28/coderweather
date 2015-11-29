package model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import db.CoderWeatherOpenHelper;

/**
 * Created by luoxn28 on 2015/11/29.
 */
public class CoderWeatherDB {
    // 数据库名
    public static final String DB_NAME = "coder_weather";
    // 数据库版本
    public static final int VERSION = 1;
    // 省级表、市级表、县级表的表名
    private static final String PROVINCE_TABLE = "Province";
    private static final String CITY_TABLE = "City";
    private static final String COUNTY_TABLE = "County";

    private static CoderWeatherDB coderWeatherDB;
    private SQLiteDatabase db;

    // 构造方法私有化
    private CoderWeatherDB(Context context) {
        CoderWeatherOpenHelper dbHelper = new CoderWeatherOpenHelper(context,
                DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    // 获取CoderWeatherDB实例
    public synchronized static CoderWeatherDB getInstance(Context context) {
        if (coderWeatherDB == null) {
            coderWeatherDB = new CoderWeatherDB(context);
        }
        return coderWeatherDB;
    }

    // 将Province实例存储到数据库
    public void saveProvince(Province province) {
        if (province != null) {
            ContentValues values = new ContentValues();

            values.put("province_name", province.getProvinceName());
            values.put("province_code", province.getProvinceCode());
            db.insert(PROVINCE_TABLE, null, values);
        }
    }

    // 从数据库中读取全国所有省份信息
    public List<Province> loadProvinces() {
        List<Province> list = new ArrayList<Province>();
        Cursor cursor = db.query(PROVINCE_TABLE, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();

                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }

        return list;
    }

    // 将City实例存储到数据库
    public void saveCity(City city) {
        if (city != null) {
            ContentValues values = new ContentValues();

            values.put("city_name", city.getCityName());
            values.put("city_code", city.getCityCode());
            values.put("province_id", city.getProvinceId());
            db.insert(CITY_TABLE, null, values);
        }
    }

    // 从数据库中读取某省份下所有城市信息
    public List<City> loadCities(int provinceId) {
        List<City> list = new ArrayList<City>();
        Cursor cursor = db.query(CITY_TABLE, null, "province_id = ?",
                new String[] { String.valueOf(provinceId)}, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                City city = new City();

                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                list.add(city);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }

        return list;
    }

    // 将County实例存储到数据库
    public void saveCounty(County county) {
        if (county != null) {
            ContentValues values = new ContentValues();

            values.put("county_name", county.getCountyName());
            values.put("county_code", county.getCountyCode());
            values.put("city_id", county.getCityId());
            db.insert(COUNTY_TABLE, null, values);
        }
    }

    // 从数据库库中读取某城市下所有县信息
    public List<County> loadCounties(int cityId) {
        List<County> list = new ArrayList<County>();
        Cursor cursor = db.query(COUNTY_TABLE, null, "city_id = ?",
                new String[] { String.valueOf(cityId)}, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                County county = new County();

                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);
                list.add(county);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }

        return list;
    }
}
