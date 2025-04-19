package edu.cs371m.wikirank.api

import com.google.gson.annotations.SerializedName

class WikiArticle (
    @SerializedName("title")
    val title: String,
    @SerializedName("extract")
    val articleExtract: String
)