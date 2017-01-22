package com.example.weizifen.coolweather.api;

import com.example.weizifen.coolweather.gson.Weather;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by weizifen on 17/1/22.
 */

public interface WeatherService {
    @GET("weather")
    Observable<Weather> mWeatherAPI(@Query("city")String city, @Query("key")String key);



    @GET("weather")
    Response<Weather> GetData(@Query("city")String city, @Query("key")String key);


}
