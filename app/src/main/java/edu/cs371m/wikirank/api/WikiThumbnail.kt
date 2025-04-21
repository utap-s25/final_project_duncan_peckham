package edu.cs371m.wikirank.api

import com.google.gson.annotations.SerializedName

data class WikiThumbnail (
    @SerializedName("title")
    val title: String,
    @SerializedName("source")
    val imageUrl: String, //unlike shortArticle, this is the full url
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    )