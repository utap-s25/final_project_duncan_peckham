package edu.cs371m.wikirank.api

import android.util.Log

class WikiArticleRepository(private val wikiApi: WikiApi) {

    private fun unpackShortArticle(response: WikiApi.WikiShortArticleResponse?): WikiShortArticle {
        val page = response?.query?.pages?.values?.firstOrNull()
        val pageTitle = page?.title ?: ""
        val props = page?.pageprops
        val item = props?.wikibaseItem ?: ""
        val shortdesc = props?.shortdesc ?: ""
        val image = props?.pageImageFree ?: ""

        return WikiShortArticle(
            title = pageTitle,
            imageUrl = image,
            shortDescription = shortdesc,
            wikiBaseItem = item
        )
    }

    suspend fun getShortArticle(title: String): WikiShortArticle{
        Log.d("getShortArticle", "Getting article $title")
        val response: WikiApi.WikiShortArticleResponse? = wikiApi.getShortArticle(title)
        val cleanedResponse = unpackShortArticle(response)
        Log.d("getShortArticle", "$cleanedResponse")
        return cleanedResponse
    }


}