package com.kp.android.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Gallery
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kp.android.photogallery.api.FlickrApi
import com.kp.android.photogallery.api.FlickrResponse
import com.kp.android.photogallery.api.PhotoInterceptor
import com.kp.android.photogallery.api.PhotoResponse
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

private const val TAG = "FlickrFetchr"

class FlickrFetchr {

    private val flickrApi: FlickrApi

    init {
        val client = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()

        //Retrofit网络请求默认都会返回一个retrofit2.Call(一个可执行的网络请求)
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.3.61:8080/")
            /*构建Retrofit对象需要指定一个scalars converter把ResponseBody对象解码为其他对象类型
            ScalarsConverterFactory.create()首先返回一个scalars converter工厂
            实例(retrofit2.converter.scalars.ScalarsConverterFactory)
            然后这个工厂实例会向Retrofit按需提供一个scalars converter实例
            在返回Call结果之前，Retrofit对象就会使用
            retrofit2.converter.scalars.StringResponseBodyConverter这个转换器
            把ResponseBody对象转换为String对象 */
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        //Retrofit会为带注解方法的接口做接口实现
        flickrApi= retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotosRequest(): Call<FlickrResponse> {
        return flickrApi.fetchPhotos()
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        //return fetchPhotoMetadata(flickrApi.fetchPhotos())
        return fetchPhotoMetadata(fetchPhotosRequest())
    }

    fun searchPhotosRequest(query: String): Call<FlickrResponse> {
        return flickrApi.searchPhotos(query)
    }

    fun searchPhotos(query: String): LiveData<List<GalleryItem>> {
        //return fetchPhotoMetadata(flickrApi.searchPhotos(query))
        return fetchPhotoMetadata(searchPhotosRequest(query))
    }

    //fun fetchPhotos(): LiveData<List<GalleryItem>> {
    private fun fetchPhotoMetadata(flickrRequest: Call<FlickrResponse>)
            : LiveData<List<GalleryItem>>{
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        //调用fetchContents()函数生成一个可执行网络请求的retrofit2.Call对象
        //val flickrRequest: Call<FlickrResponse> = flickrApi.fetchPhotos()
        //把抓取Flickr主页的网络请求加入任务队列，并立即返回responseLiveData
        flickrRequest.enqueue(object : Callback<FlickrResponse> {

            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }

            override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
                Log.d(TAG, "Response received")
                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                responseLiveData.value = galleryItems
            }
        })

        return responseLiveData
    }

    @WorkerThread//表示函数只能在后台线程上执行
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        /*使用ResponseBody.byteStream()函数从响应数据里取出java.io.InputStream
        再传入BitmapFactory.decodeStream(InputStream)供其创建Bitmap对象
        InputStream实现了Closeable接口，因此标准函数use(...)会在
        BitmapFactory.decodeStream(...)函数返回值后关闭响应流和字节流*/
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decode bitmap=$bitmap from Response=$response")
        return bitmap
    }
}