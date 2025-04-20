package edu.cs371m.wikirank.api

import android.text.SpannableString
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.lang.reflect.Type

interface WikiApi {

    //todo add another endpoint call to get more detailed data about each article
    //example url https://en.wikipedia.org/w/api.php?action=query&titles=Albert_Einstein&prop=pageprops&format=json
    @GET("/w/api.php?action=query&prop=pageprops&format=json")
    suspend fun getShortArticle(@Query("titles") title: String) : WikiShortArticleResponse?

    @GET("/w/api.php?action=query&prop=extracts&format=json&explaintext=true")
    suspend fun getFullArticle(@Query("titles") title: String): WikiFullArticleResponse?

    data class WikiFullArticleResponse(val query: WikiQuery)

    data class WikiShortArticleResponse(val query: WikiQuery)

    data class WikiQuery(val pages: Map<String, Page>?)

    data class Page(
        val pageid: Int?,
        val title: String?,
        val pageprops: PageProps?,
        val extract: String?
    )

    data class PageProps(
        @SerializedName("wikibase_item")
        val wikibaseItem: String?,

        @SerializedName("wikibase-shortdesc")
        val shortdesc: String?,

        @SerializedName("page_image_free")
        val pageImageFree: String?
    )


    companion object {
        private fun buildGsonConverterFactory(): GsonConverterFactory{
            val gsonBuilder = GsonBuilder()
            return GsonConverterFactory.create(gsonBuilder.create())
        }
        var httpurl = HttpUrl.Builder()
            .scheme("https")
            .host("en.wikipedia.org")
            .build()
        fun create(): WikiApi = create(httpurl)
        private fun create(httpUrl: HttpUrl): WikiApi{
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    // Enable basic HTTP logging to help with debugging.
                    this.level = HttpLoggingInterceptor.Level.BASIC
                })
                .build()
            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(buildGsonConverterFactory())
                .build()
                .create(WikiApi::class.java)
        }
    }
}