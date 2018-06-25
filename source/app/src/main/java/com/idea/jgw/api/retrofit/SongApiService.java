package com.idea.jgw.api.retrofit;


import com.idea.jgw.bean.BaseResponse;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by Ganlin.Wu on 2016/9/22.
 */
public interface SongApiService {
    String BASE_URL = "http://120.132.120.251:10004/";

    @FormUrlEncoded
    @POST("register")
    Observable<BaseResponse> register(@FieldMap HashMap<String, String> map);

    @FormUrlEncoded
    @POST("sendsms")
    Observable<BaseResponse> sendsms(@Field ("phone") String phone);

    @FormUrlEncoded
    @POST("findpwdsms")
    Observable<BaseResponse> findpwdsms(@Field ("token") String token);

    @FormUrlEncoded
    @POST("findpwd")
    Observable<BaseResponse> findpwd(@Field ("token") String token, @Field ("passwd") String passwd, @Field ("verifycode") String verifycode);

    @FormUrlEncoded
    @POST("editinfo")
    Observable<BaseResponse> editinfo(@Field ("token") String token, @Field ("nickname") String nickname, @Field ("sex") int sex);

    @FormUrlEncoded
    @POST("feedback")
    Observable<BaseResponse> feedback(@Field ("token") String token, @Field ("content") String content, @Field ("contact") String contact);

    @FormUrlEncoded
    @POST("http://120.132.120.251:10004/getinfo")
    Observable<BaseResponse> getinfo(@Field ("token") String token);

    @FormUrlEncoded
    @POST("login")
    Observable<BaseResponse> login(@FieldMap HashMap<String, String> map);

    @Multipart
    @POST("edit_face")
//    Observable<BaseResponse> updatePhoto(@PartMap() Map<String, RequestBody> files);
    Observable<BaseResponse> updatePhoto(@Part("token") RequestBody token, @Part MultipartBody.Part file);
//    Observable<BaseResponse> updatePhoto(@Part("token") String token, @Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST("http://120.132.120.251:8080/cal_record")
    Observable<BaseResponse> calRecord(@Field("token") String token, @Field("page") int page);

    @FormUrlEncoded
    @POST("http://120.132.120.251:8080/book")
    Observable<BaseResponse> miningList(@Field("cointype") int coinType, @Field("token") String token, @Field("page") int page);

    @FormUrlEncoded
    @POST("http://120.132.120.251:8080/profit")
    Observable<BaseResponse> miningData(@Field("device_code") String device_code, @Field("token") String token);

    @FormUrlEncoded
    @POST("http://120.132.120.251:8080/receive")
    Observable<BaseResponse> receiveMiningData(@Field("cointype") int coinType, @Field("num") String num, @Field("token") String token);

    @FormUrlEncoded
    @POST("http://120.132.120.251:8080/transfer")
    Observable<BaseResponse> transferMiningData(@Field("cointype") int coinType, @Field("tradepwd") String tradepwd, @Field("num") double num, @Field("addr") String addr);

//    @FormUrlEncoded
//    @POST("register")
//    Observable<BaseResponse> register(@Field("account") String account, @Field("passwd") String passwd, @Field("verifycode") String verifycode);
//    @POST("register")
//    Observable<BaseResponse> register(@Body RegisterRequest registerRequest);

//    @GET("v1/restserver/ting")
//    Observable<PlayBean> getPlay(@Query("format") String format, @Query("callback") String callback, @Query("from") String from,
//                                 @Query("method") String method, @Query("songid") String songid);
//
//    @GET("v1/restserver/ting")
//    Observable<SongDownloadBean> getDownloadSong(@Query("format") String format, @Query("callback") String callback, @Query("from") String from,
//                                                 @Query("method") String method, @Query("songid") long songid, @Query("bit") int bit, @Query("_t") long t);

}
