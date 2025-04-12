package edu.cs371m.wikirank.api

class WikiArticleRepository(private val wikiApi: WikiApi) {

    private fun unpackShortArticle(response: WikiApi.WikiShortArticleResponse): WikiShortArticle {
        // XXX Write me.
        return response.result
    }

    suspend fun getShortArticle(title: String): WikiShortArticle{
        val response: WikiApi.WikiShortArticleResponse? = wikiApi.getShortArticle(title)
        return unpackShortArticle(response!!)
    }
}