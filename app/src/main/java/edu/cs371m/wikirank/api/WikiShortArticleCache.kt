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
    fun put(key: String, article: WikiShortArticle) {
        cache[key] = article
    }

    /** Convenience for bulk lookups. */
    @Synchronized
    fun getMany(keys: List<String>): Pair<Map<String, WikiShortArticle>, List<String>> {
        val hit  = mutableMapOf<String, WikiShortArticle>()
        val miss = mutableListOf<String>()
        keys.forEach { k -> if (cache[k] != null) hit[k] = cache[k]!! else miss += k }
        return hit to miss
    }

}
