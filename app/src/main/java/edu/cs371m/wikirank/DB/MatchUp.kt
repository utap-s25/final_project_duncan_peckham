package edu.cs371m.wikirank.DB

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

enum class Vote{
    ARTICLE_ONE,
    ARTICLE_TWO
}
data class MatchUp (
    val articleOne: String = "",
    val articleTwo: String = "",
    val userId: String = "",
    val vote: Vote = Vote.ARTICLE_ONE,

    @ServerTimestamp var timestamp: Timestamp? = null,
    @DocumentId var firestoreID: String = ""
)