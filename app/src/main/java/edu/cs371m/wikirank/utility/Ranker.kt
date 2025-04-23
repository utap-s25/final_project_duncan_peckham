package edu.cs371m.wikirank.utility

import android.util.Log
import edu.cs371m.wikirank.DB.MatchUp
import edu.cs371m.wikirank.DB.Vote
import edu.cs371m.wikirank.api.WikiShortArticle
import kotlin.math.pow


data class RankedObject(
    val id: String,
    val rating: Double
)

data class RatedArticle(
    val article: WikiShortArticle,
    val rating: Double,
    val firestoreID: String
)

class Ranker(
    private val initialArticles: List<String>, // firestore IDs, not titles
    private val initialMatchupList: List<MatchUp>
) {
    private val K = 32

    private val articles: MutableList<String> = initialArticles.toMutableList()
    private val ratings = mutableMapOf<String, Double>().apply{
        articles.forEach{ this[it] = 1500.0}
    }

    private val matchupList: MutableList<MatchUp> = initialMatchupList.toMutableList()

    fun getList(): List<String> = articles

    fun getRating(id: String): Double = ratings[id] ?: 1500.0

    fun getRankedObjects(): List<RankedObject>{
        sortList()
        return articles.map { id -> RankedObject(id, ratings[id] ?: 1500.0)}
    }

    // ELO Implementation
    // For each matchup, calculate expectations of each player (1 + 10^(R_B - R_A)/480)^(-1)
    // Then update the score R_A' = R_A + K(S_A - E_A) were S_A is the actual score for A, K is 32.

    fun processMatchup(matchUp: MatchUp){
        val id1 = matchUp.articleOne
        val id2 = matchUp.articleTwo

        if (id1 !in articles) articles += id1
        if (id2 !in articles) articles += id2

        val r1 = ratings.getOrPut(id1){ 1500.0}
        val r2 = ratings.getOrPut(id2){ 1500.0}

        val e1 = (1 + 10.0.pow((r2 - r1) / 480)).pow(-1)
        val e2 = 1 - e1

        val s1 = if(matchUp.vote == Vote.ARTICLE_ONE) 1 else 0
        val s2 = 1 - s1

        ratings[id1] = r1 + K * (s1 - e1)
        ratings[id2] = r2 + K * (s2 - e2)
        Log.d("processMatchup", "Updated article $id1 by ${ K * (s1 - e1)}, $id2 by ${ K * (s2 - e2)}")
    }

    fun addMatchups(matchups: List<MatchUp>){
        matchups.forEach {
            if(matchupList.contains(it)) return@forEach
            matchupList.add(it)
            processMatchup(it)
        }
    }

    fun resetArticles(ids: List<String>) {
        ids.filter{ it !in articles}.forEach {
            articles += it
            ratings.getOrPut(it) {1500.0}
        }
    }

    //Asked chatGPT how to sort this list with respect to the ratings list
    private fun sortList(){
        articles.sortWith(compareByDescending { ratings[it] ?: 1500.0 })
    }

    private fun processAllMatchups(){
        matchupList.forEach { processMatchup(it) }
    }

    init{
        processAllMatchups()
        sortList()
    }

}