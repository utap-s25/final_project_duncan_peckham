package edu.cs371m.wikirank.api

import android.util.Log

class WikiArticleRepository(private val wikiApi: WikiApi) {

    private fun unpackShortArticle(response: WikiApi.WikiArticleResponse?): WikiShortArticle? {
        val pages = response?.query?.pages
        val page = pages?.filter { it.value.index == 1 }?.values?.firstOrNull()
        val pageTitle = page?.title ?: return null
        val props = page.pageprops
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

    private fun unpackArticle(response: WikiApi.WikiArticleResponse?): WikiArticle? {
        val page = response?.query?.pages?.values?.firstOrNull()
        val pageTitle = page?.title ?: return null
        val articleExtract = page.extract ?: ""

        return WikiArticle(
            title = pageTitle,
            articleExtract = articleExtract
        )
    }

    private fun unpackThumbnail(response: WikiApi.WikiArticleResponse?): WikiThumbnail? {
        val page = response?.query?.pages?.values?.firstOrNull()
        val title = page?.title ?: return null
        val thumbnail = page.thumbnail ?: return null

        return WikiThumbnail(
            title = title,
            imageUrl = thumbnail.source ?: return null,
            width = thumbnail.width ?: 0,
            height = thumbnail.height ?: 0
        )
    }


    suspend fun getShortArticle(title: String): WikiShortArticle?{
        Log.d("getShortArticle", "Getting article $title")
        val response: WikiApi.WikiArticleResponse? = wikiApi.getShortArticle(title)
        val cleanedResponse = unpackShortArticle(response)
        Log.d("getShortArticle", "$cleanedResponse")
        return cleanedResponse
    }

    suspend fun getArticle(title: String): WikiArticle?{
        Log.d("getArticle", "Getting article $title")
        val response: WikiApi.WikiArticleResponse? = wikiApi.getFullArticle(title)
        val cleanedResponse = unpackArticle(response)
        Log.d("getArticle", "$cleanedResponse")
        return cleanedResponse
    }

    suspend fun getThumbnail(title: String, size: Int): WikiThumbnail? {
        Log.d("getThumbnail", "Getting thumbnail for $title with size $size")
        val response: WikiApi.WikiArticleResponse? = wikiApi.getThumbnail(title, size)
        val cleaned = unpackThumbnail(response)
        Log.d("getThumbnail", "$cleaned")
        return cleaned
    }

}