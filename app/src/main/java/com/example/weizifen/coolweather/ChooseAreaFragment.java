package com.example.weizifen.coolweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weizifen.coolweather.db.City;
import com.example.weizifen.coolweather.db.County;
import com.example.weizifen.coolweather.db.Province;
import com.example.weizifen.coolweather.util.HttpUtil;
import com.example.weizifen.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by weizifen on 17/1/20.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String>dataList=new ArrayList<>();
    /*
* 省份列表
* */
    private List<Province>provinceList;


/*
* 城市列表
* */
    private List<City>cityList;


/*
* 县级市列表
* */
    private List<County>countyList;

/*
选中的省份
* */
    private Province selectedProvince;

/*
*选中的城市
 *  */
    private City selectedCity;

/*
* 选中的级别
* */
    private int currentLevel;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView) view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);



        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel==LEVEL_PROVINCE)
                {
                    selectedProvince=provinceList.get(i);
                    queryCitys();
                }
                else if (currentLevel==LEVEL_CITY)
                {
                   selectedCity=cityList.get(i);
                    queryCounties();
                }else if (currentLevel==LEVEL_COUNTY)
                {
                    String weatherCity=countyList.get(i).getCountyName();
                    Intent intent=new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_city",weatherCity);
                    startActivity(intent);
                    getActivity().finish();
                }

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),provinceList.get(0).getProvinceName(),Toast.LENGTH_SHORT).show();
                if (currentLevel==LEVEL_COUNTY)
                {
                    queryCitys();;
                }else if (currentLevel==LEVEL_CITY)
                {
                    queryProvinces();
                }
            }
        });
        queryProvinces();


    }
    /*
* 查询省份
* */
    public void  queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);


        if (provinceList.size()>0)
        {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;

        }
        else
        {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    /*
    * 查询城市
    * */
    private void queryCitys(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0)
        {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            currentLevel=LEVEL_CITY;
            listView.setSelection(0);

        }
        else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }

    }
    /*
    * 查询查询县*/
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);

        if (countyList.size()>0)
        {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());

            }
            adapter.notifyDataSetChanged();
            currentLevel=LEVEL_COUNTY;
            listView.setSelection(0);

        }else
        {
            int provinceCode=selectedProvince.getProvinceCode();
            int citycode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+citycode;
            queryFromServer(address,"county");

        }
    }

    private static final String TAG = "ChooseAreaFragment";

    /*根据传入的类型地址从服务器上查询数据*/
    private  void queryFromServer(String address, final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getActivity(),"加载失败",Toast
                        .LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                Log.d(TAG, responseText);
                boolean result=false;
                if ("province".equals(type))
                {
                    result= Utility.handlProvinceResponse(responseText);
                }else  if ("city".equals(type))
                {
                    result= Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else  if ("county".equals(type))
                {
                    result= Utility.handleCountyResponse(responseText,selectedCity.getId());

                }

                if (result)
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            closeProgressDialog();
                            if ("province".equals(type))
                            {
                                queryProvinces();
                            }else if ("city".equals(type))
                            {
                                queryCitys();
                            }else if ("county".equals(type))
                            {
                                queryCounties();
                            }

                        }
                    });
                }

            }
        });



    }




    /*加载*/
    private void showProgressDialog(){
        if (progressDialog==null)
        {
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("加载中");
            progressDialog.setCanceledOnTouchOutside(false);

        }
        progressDialog.show();
    }
    /*加载圈圈取消*/
    private  void closeProgressDialog(){
        if (progressDialog!=null)
        {
            progressDialog.dismiss();
        }
    }



}
