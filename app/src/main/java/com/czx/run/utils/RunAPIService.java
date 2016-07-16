package com.czx.run.utils;

import com.czx.run.MyApplication;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.OkHeaders;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by czx on 2016/7/1.
 */
public class RunAPIService {

    private static volatile RunAPI runAPI;

    private static Retrofit retrofit;

    private static Retrofit getRetrofit(){

        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                LogUtils.i(getClass(),"in intercept");
                Request request = chain.request();
                if(!AccessUtils.isNetworkConnected(MyApplication.getContext())){
                    LogUtils.i(getClass(),"no network,force cache");
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }
                Response response = chain.proceed(request);
                if(AccessUtils.isNetworkConnected(MyApplication.getContext())){
                    int maxAge = 60*60;
                    response.newBuilder()
                            .header("Cache-Control","public,max-age="+maxAge)
                            .build();
                }else{
                    int maxScale = 60 * 60 * 24 *28;
                    LogUtils.i(getClass(),"no network,public max-stale");
                    response.newBuilder()
                            .header("Cache-Control","public,max-stale+"+maxScale)
                            .build();
                }
                return response;
            }
        };

        File httpCacheDir = new File(MyApplication.getContext().getCacheDir(),"responses");
        Cache cache = new Cache(httpCacheDir,1024 * 1024 * 10);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .cache(cache)
                .build();


        retrofit = new Retrofit.Builder()
                .baseUrl(RunAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();

        return retrofit;
    }

    private RunAPIService(){}

    public static RunAPI getInstance(){
        if(runAPI == null){
            synchronized (RunAPIService.class){
                if(runAPI  == null){
                    runAPI =  getRetrofit().create(RunAPI.class);
                }
            }
        }
        return runAPI;
    }
}
