package edu.cs371m.wikirank.utility

import edu.cs371m.wikirank.DB.MatchUp
import edu.cs371m.wikirank.DB.Vote
import kotlin.math.pow


data class RankedObject(
    val id: String,
    val rating: Double
)
class Ranker(
    private val initialArticles: List<String>,
    private val initialMatchupList: List<MatchUp>
) {
    private val K = 32

    private val articles: MutableList<String> = initialArticles.toMutableList()
    private val ratings = mutableMapOf<String, Double>().apply{
        articles.forEach{ this[it] = 1500.0}
    }

    private val matchupList: MutableList<MatchUp> = initialMatchupList.toMutableList()

    fun getList(): List<String> = articles

    // ELO Implementation
    // For each matchup, calculate expectations of each player (1 + 10^(R_B - R_A)/480)^(-1)
    // Then update the score R_A' = R_A + K(S_A - E_A) were S_A is the actual score for A, K is 32.

    fun processMatchup(matchUp: MatchUp){
        val articleOne = matchUp.articleOne
        val articleTwo = matchUp.articleTwo

        val r1 = ratings.getOrPut(articleOne){ 1500.0}
        val r2 = ratings.getOrPut(articleTwo){ 1500.0}

        val e1 = (1 + 10.0.pow((r2 - r1) / 480)).pow(-1)
        val e2 = 1 - e1

        val s1 = if(matchUp.vote == Vote.ARTICLE_ONE) 1 else 0
        val s2 = 1 - s1

        ratings[articleOne] = r1 + K * (s1 - e1)
        ratings[articleTwo] = r2 + K * (s2 - e2)
    }

    fun addMatchup(matchUp: MatchUp){
        if(matchupList.contains(matchUp)) return
        matchupList.add(matchUp)
        processMatchup(matchUp)
        sortList()

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