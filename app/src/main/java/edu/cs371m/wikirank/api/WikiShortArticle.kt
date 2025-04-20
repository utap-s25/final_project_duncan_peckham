package edu.cs371m.wikirank.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WikiShortArticle (
    @SerializedName("title")
    val title: String,
    @SerializedName("page_image_free")
    private val imageUrl: String,
    @SerializedName("wikibase-shortdesc")
    val shortDescription: String,
    @SerializedName("wikibase_item")
    val wikiBaseItem: String
): Serializable {
    fun getImageUrl(): String{
        return "https://commons.wikimedia.org/wiki/Special:FilePath/${this.imageUrl}"
    }
}