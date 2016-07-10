package com.czx.run.utils;

import com.czx.run.model.RecordResult;
import com.czx.run.model.CommonResult;
import com.czx.run.model.LoginResult;
import com.czx.run.model.MaxIdResult;
import com.czx.run.model.GroupRecordResult;
import com.czx.run.model.SumResult;
import com.czx.run.model.UserResult;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by czx on 2016/7/1.
 */
public interface RunAPI {

    public static final String BASE_URL = "http://codeczx.duapp.com/FitServer/";



    @GET("UserServlet?method=login")
    Observable<LoginResult> login(@Query("email")String email, @Query("password")String password);

    @GET("UserServlet?method=getUser")
    Observable<UserResult> getUser(@Query("token") String token);

    @GET("UserServlet?method=register")
    Observable<CommonResult> register(@Query("email")String email, @Query("password")String password, @Query("name")String name);

    @GET("RunRecordServlet?method=findsum")
    Observable<SumResult> getSumData(@Query("token")String token);

    @GET("RunRecordServlet?method=findall")
    Observable<RecordResult> findAll(@Query("token") String token);

    @GET("UserServlet?method=setWeight")
    Observable<CommonResult> setWeigth(@Query("token") String token, @Query("weight") String weight);

    @FormUrlEncoded
    @POST("RunRecordServlet")
    Observable<CommonResult> add(@Field("method") String method, @Field("token") String token,
                                 @Field("date") String date, @Field("distance") String distance,
                                 @Field("calorie") String calorie, @Field("runtime") String runTime,
                                 @Field("pointskey") String pointskey, @Field("address") String address);

    @GET("RunRecordServlet?method=findmaxid")
    Observable<MaxIdResult> getMaxId();

    @GET("UserServlet?method=setPhoto")
    Observable<CommonResult> updatePhotoInfo(@Query("photo") String photoKey, @Query("token") String token);

    @GET("RunRecordServlet?method=getrecord")
    Observable<GroupRecordResult> getRecordsById(@Query("id") int id,
                                                 @Query("token") String token);
}
