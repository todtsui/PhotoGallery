package com.kp.android.photogallery.api

import com.google.gson.annotations.SerializedName
import com.kp.android.photogallery.GalleryItem

class PhotoResponse {
    @SerializedName("photo") //储存photo图片集合
    lateinit var galleryItems: List<GalleryItem>
}