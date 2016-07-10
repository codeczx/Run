package com.czx.run.utils;

import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by czx on 2016/7/1.
 */
public class RunAPIService {

    private static volatile RunAPI runAPI;

    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(RunAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create()))
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .build();

    private RunAPIService(){}

    public static RunAPI getInstance(){
        if(runAPI == null){
            synchronized (RunAPIService.class){
                if(runAPI  == null){
                    runAPI =  retrofit.create(RunAPI.class);
                }
            }
        }
        return runAPI;
    }
}
