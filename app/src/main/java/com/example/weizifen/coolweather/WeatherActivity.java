package com.example.weizifen.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.weizifen.coolweather.api.WeatherService;
import com.example.weizifen.coolweather.gson.Weather;
import com.example.weizifen.coolweather.util.HttpUtil;
import com.example.weizifen.coolweather.util.Utility;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class WeatherActivity extends AppCompatActivity {

    @BindView(R.id.title_city)
    TextView titleCity;
    @BindView(R.id.title_update_time)
    TextView titleUpdateTime;
    @BindView(R.id.degree_text)
    TextView degreeText;
    @BindView(R.id.weather_info_text)
    TextView weatherInfoText;

    @BindView(R.id.aqi_text)
    TextView aqiText;
    @BindView(R.id.pm25_text)
    TextView pm25Text;
    @BindView(R.id.comfort_text)
    TextView comfortText;
    @BindView(R.id.car_wash_text)
    TextView carWashText;
    @BindView(R.id.sport_text)
    TextView sportText;
    @BindView(R.id.weather_layout)
    ScrollView weatherLayout;
    @BindView(R.id.bing_pic_img)
    ImageView bingPicImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*去除状态栏方法*/
        if (Build.VERSION.SDK_INT>=21)
        {
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN|View.TEXT_ALIGNMENT_VIEW_START
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);

        }
        /*在加载布局前设置*/
        setContentView(R.layout.activity_weather);
        ButterKnife.bind(this);


        /*-------------------------------------------------------------------------*/
        /*p205*/
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        String weatherString = prefs.getString("weather", null);
//        Log.d(TAG, weatherString);
//        if (weatherString != null) {
//            Weather weather = Utility.handleWeatherResponse(weatherString);
//            showWeatherInfo(weather);
//        }else {
//            String weatherCity=getIntent().getStringExtra("weather_city");
//            Log.d(TAG, weatherCity);
//            weatherLayout.setVisibility(View.INVISIBLE);
////            requestWeather(weatherCity);
//            rxjavaAndRetrofit(weatherCity);
//        }

        /*-------------------------------------------------------------------------*/
        String weatherCity = getIntent().getStringExtra("weather_city");
        Log.d(TAG, weatherCity);
        rxjavaAndRetrofit(weatherCity);
        loadingBackgroundImage();


    }

    /*=============================================================================*/
    /*--------------------------rxjava与rxandroid获取网络请求----------------------*/
    private void rxjavaAndRetrofit(final String weatherCity) {
        String responseText;
        String weatherUrl = "https://api.heweather.com/v5/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(weatherUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherService weatherApi = retrofit.create(WeatherService.class);
        weatherApi.mWeatherAPI(weatherCity, "bf063117876546e78591665799355973")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Weather>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Weather weather) {
                        if (weather != null && "ok".equals(weather.getHeWeather5().get(0).getStatus())) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", weather.toString());
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                        }
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("weather", weather.getHeWeather5().toString());
                        Log.d(TAG, retrofit2.Response.success(weather).toString());
                        editor.apply();
                        showWeatherInfo(weather);
                        forcastInfo(weather);
                    }
                });
    }
    /*--------------------------rxjava与rxandroid获取网络请求----------------------*/
    /*=============================================================================*/


    /*============================================================*/
    /*--------------------------被我废弃使用----------------------*/
    private static final String TAG = "WeatherActivity";

    private void requestWeather(final String weatherCity) {
        /*通过城市名或者城市ID获取数据*/
        String weatherUrl = "https://api.heweather.com/v5/weather?city=" + weatherCity + "&&key=bf063117876546e78591665799355973";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.d(TAG, responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
    }
    /*--------------------------被我废弃使用----------------------*/
    /*============================================================*/


    /*============================================================*/
    /*--------------------------天气相关-------------------------*/
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.getHeWeather5().get(0).getBasic().getCity();
        String updateTime = weather.getHeWeather5().get(0).getBasic().getUpdate().getUtc();
        String degree = weather.getHeWeather5().get(0).getNow().getTmp() + "℃";
        String weatherInfo = weather.getHeWeather5().get(0).getNow().getCond().getTxt();
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        aqiText.setText(weather.getHeWeather5().get(0).getAqi().getCity().getAqi());
        pm25Text.setText(weather.getHeWeather5().get(0).getAqi().getCity().getPm25());
        String comfort = "舒适度:" + weather.getHeWeather5().get(0).getSuggestion().getComf().getTxt();
        String carWash = "洗车指数" + weather.getHeWeather5().get(0).getSuggestion().getCw().getTxt();
        String sport = "运动建议:" + weather.getHeWeather5().get(0).getSuggestion().getSport().getTxt();
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
    }
    /*--------------------------天气相关-------------------------*/
    /*============================================================*/


    /*============================================================*/
    /*--------------------------未来天气-------------------------*/
    private void forcastInfo(Weather weather) {

        for (Weather.HeWeather5Bean heWeather5Bean : weather.getHeWeather5()) {
            for (Weather.HeWeather5Bean.DailyForecastBean dailyForecastBean : heWeather5Bean.getDaily_forecast()) {
                {
                    LinearLayout forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
                    Toast.makeText(WeatherActivity.this, dailyForecastBean.getDate(), Toast.LENGTH_SHORT).show();
                    View view = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.forecast_item, forecastLayout, false);
                    TextView dateText = (TextView) view.findViewById(R.id.date_text);
                    TextView infoText = (TextView) view.findViewById(R.id.info_text);
                    TextView maxText = (TextView) view.findViewById(R.id.max_text);
                    TextView minText = (TextView) view.findViewById(R.id.min_text);
                    dateText.setText(dailyForecastBean.getDate());
                    infoText.setText(dailyForecastBean.getCond().getTxt_d());
                    maxText.setText(dailyForecastBean.getTmp().getMax());
                    minText.setText(dailyForecastBean.getTmp().getMin());
                    forecastLayout.addView(view);

                }


            }
            weatherLayout.setVisibility(View.VISIBLE);


        }
    }
    /*--------------------------未来天气-------------------------*/
    /*============================================================*/


     /*============================================================*/
     /*--------------------------背景图片-------------------------*/
    private void loadingBackgroundImage() {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(WeatherActivity.this).load("http://cn.bing.com/az/hprichbg/rb/PfeifferBeach_ZH-CN13868196659_1920x1080.jpg").into(bingPicImg);
            }
        });



    }
     /*--------------------------未来天气-------------------------*/
     /*============================================================*/

}
