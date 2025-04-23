package edu.cs371m.wikirank.utility

import edu.cs371m.wikirank.DB.MatchUp


data class RankedObject(
    val id: String,
    val rating: Double
)
class Ranker(
    private val articleList: List<String>,
    private val matchupList: List<MatchUp>
) {
    private val ratings = mutableMapOf<String, Double>().apply{
        articleList.forEach{ this[it] = 1500.0}
    }

    fun getList(): List<String>{
        return articleList
    }

    // todo: ELO Implementation
    // For each matchup, calculate expectations of each player (1 + 10^(R_B - R_A)/480)^(-1)
    // Then update the score R_A' = R_A + K(S_A - E_A) were S_A is the actual score for A, K is 32.
}