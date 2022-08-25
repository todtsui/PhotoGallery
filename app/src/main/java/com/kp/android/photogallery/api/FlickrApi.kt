package com.kp.android.photogallery.api

import androidx.annotation.WorkerThread
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface FlickrApi {

    //@GET注解把fetchContents()函数返回的Call配置成一个GET请求
    @GET("photo/photos")
    //执行Retrofit网络请求返回一个retrofit2.Call对象
    //执行Call网络请求返回一个相应的HTTP响应
    //Retrofit默认把HTTP响应数据反序列化为一个OkHttp.ResponseBody 对象
    //Retrofit将OkHttp.ResponseBody对象转化为Call的泛型String
    fun fetchPhotos(): Call<FlickrResponse>

    @GET
    //使用传入的URL参数来决定从哪里下载数
    //Retrofit默认会把网络响应数据反序列化为OkHttp3.ResponseBody对象
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>

    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query: String): Call<FlickrResponse>

}