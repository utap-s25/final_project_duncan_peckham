package edu.cs371m.wikirank.api

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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


    private suspend fun getShortArticle(title: String): WikiShortArticle?{
        Log.d("getShortArticle", "Getting article $title")
        val response: WikiApi.WikiArticleResponse? = wikiApi.getShortArticle(title)
        val cleanedResponse = unpackShortArticle(response)
        Log.d("getShortArticle", "$cleanedResponse")
        return cleanedResponse
    }

    //chatGpt helped write these cached versions of these calls
    suspend fun getShortArticleCached(title: String): WikiShortArticle? {
        ShortArticleCache.get(title)?.let {
            Log.d(javaClass.simpleName, "Cache hit $title")
            return it } //cache hit
        val missed = getShortArticle(title)
        if( missed != null) {
            Log.d(javaClass.simpleName, "Cache miss $title")
            ShortArticleCache.put(title, missed)
        }
        return missed
    }

    /** Core helper: fetch and return a Map keyed by the *request* strings. */
    suspend fun getShortArticlesMap(keys: List<String>): Map<String, WikiShortArticle> {
        val (hits, missKeys) = ShortArticleCache.getMany(keys)   // hits already keyed by request
        if (missKeys.isEmpty()) return hits                      // all cached

        val fetched = coroutineScope {
            missKeys.map { k -> async { k to getShortArticle(k) } }.awaitAll()
        }.filter { it.second != null } as List<Pair<String, WikiShortArticle>>

        fetched.forEach { (k, art) -> ShortArticleCache.put(k, art) }

        return hits + fetched.toMap()        // still keyed by request strings
    }

    /** Thin wrapper for callers that still expect a List. */
    suspend fun getShortArticlesCached(keys: List<String>): List<WikiShortArticle> =
        getShortArticlesMap(keys).values.toList()

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