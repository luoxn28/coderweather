package activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luoxn28.coderweather.R;

import java.util.ArrayList;
import java.util.List;

import model.City;
import model.CoderWeatherDB;
import model.County;
import model.Province;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

/**
 * Created by luoxn28 on 2015/11/29.
 */
public class ChooseAreaActivity extends Activity {
    // 当前活动所处级别
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private TextView titleText;
    private ListView listView;

    private ProgressDialog progressDialog;
    private ArrayAdapter<String> adapter;
    private CoderWeatherDB coderWeatherDB;
    private List<String> dataList = new ArrayList<String>();

    // 省级列表、市级列表、县级列表
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    // 选中的省份、城市
    private Province selectedProvince;
    private City selectedCity;

    // 当前选中的级别
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);

        titleText = (TextView) findViewById(R.id.title_text);
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        coderWeatherDB = CoderWeatherDB.getInstance(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });

        // 加载省级数据
        queryProvince();
    }

    // 查询全国所有的省，优先从数据库查询，如果没有查到再去服务器上查询
    private void queryProvince() {
        provinceList = coderWeatherDB.loadProvinces();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }
        else {
            queryFromServer(null, "province");
        }
    }

    // 查询省内所有的市，优先从数据库查询，如果没有查到再去服务器上查询
    private void queryCities() {
        cityList = coderWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }
        else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    // 查询选中市内所有的县，优先从数据库查询，如果没有查到再去服务器上查询
    private void queryCounties() {
        countyList = coderWeatherDB.loadCounties(selectedCity.getId());
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }
        else {
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    // 根据传入的代号和类型从服务器上查询省市县数据
    private void queryFromServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        }
        else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvincesResponse(coderWeatherDB, response);
                }
                else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(coderWeatherDB, response, selectedProvince.getId());
                }
                else if ("county".equals(type)) {
                    result = Utility.handleCountiesResponse(coderWeatherDB, response, selectedCity.getId());
                }

                if (result) {
                    // 通过runOnTiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvince();
                            }
                            else if ("city".equals(type)) {
                                queryCities();
                            }
                            else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception ex) {
                // 通过runOnTiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // 显示进度对话框
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在玩命加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    // 关闭进度对话框
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    // 捕获Back按键，更具当前级别来判断，此时应该返回市列表、省列表，还是直接退出
    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY) {
            queryCities();
        }
        else if (currentLevel == LEVEL_CITY) {
            queryProvince();
        }
        else {
            finish();
        }
    }
}
