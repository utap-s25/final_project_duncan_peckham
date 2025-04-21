package edu.cs371m.wikirank.api

import android.util.Log

class WikiArticleRepository(private val wikiApi: WikiApi) {

    private fun unpackShortArticle(response: WikiApi.WikiShortArticleResponse?): WikiShortArticle {
        val pages = response?.query?.pages
        val page = pages?.filter { it.value.index == 1 }?.values?.firstOrNull()
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

    private fun unpackArticle(response: WikiApi.WikiFullArticleResponse?): WikiArticle{
        val page = response?.query?.pages?.values?.firstOrNull()
        val pageTitle = page?.title ?: ""
        val articleExtract = page?.extract ?: ""

        return WikiArticle(
            title = pageTitle,
            articleExtract = articleExtract
        )
    }

    suspend fun getShortArticle(title: String): WikiShortArticle{
        Log.d("getShortArticle", "Getting article $title")
        val response: WikiApi.WikiShortArticleResponse? = wikiApi.getShortArticle(title)
        val cleanedResponse = unpackShortArticle(response)
        Log.d("getShortArticle", "$cleanedResponse")
        return cleanedResponse
    }

    suspend fun getArticle(title: String): WikiArticle{
        Log.d("getArticle", "Getting article $title")
        val response: WikiApi.WikiFullArticleResponse? = wikiApi.getFullArticle(title)
        val cleanedResponse = unpackArticle(response)
        Log.d("getArticle", "$cleanedResponse")
        return cleanedResponse
    }

}