package edu.cs371m.wikirank.api

import android.util.Log

// generated from a chat GPT request to create a cache that would reduce the number of API requests I had to make
/**
 * Thread-safe LRU cache for WikiShortArticle objects.
 *
 * • MAX_SIZE keeps memory bounded.
 * • accessOrder = true makes the LinkedHashMap act as an LRU.
 * • All methods are synchronized → safe for multi-threaded coroutines.
 */
object ShortArticleCache {
    private const val MAX_SIZE = 300        // tune to taste

    private val cache: LinkedHashMap<String, WikiShortArticle> =
        object : LinkedHashMap<String, WikiShortArticle>(MAX_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, WikiShortArticle>?): Boolean {
                return size > MAX_SIZE      // kick out least-recently-used entry
            }
        }

    @Synchronized
    fun get(title: String): WikiShortArticle? = cache[title]

    @Synchronized
    fun put(article: WikiShortArticle) {
        cache[article.title] = article
    }

    /** Convenience for bulk lookups. */
    @Synchronized
    fun getMany(titles: List<String>): Pair<List<WikiShortArticle>, List<String>> {
        val hits   = mutableListOf<WikiShortArticle>()
        val missTs = mutableListOf<String>()
        titles.forEach { t ->
            val hit = cache[t]
            if (hit != null){
                Log.d(javaClass.simpleName, "Cache hit in getMany ${hit.title}")
                hits.add(hit)}
            else{
                Log.d(javaClass.simpleName, "Cache miss in getMany $t")
                missTs.add(t)}
        }
        return hits to missTs
    }
}
