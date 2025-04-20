package edu.cs371m.wikirank.DB

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DBArticle (
    var name: String = "",
    var category: String = "",
    var order_id: Int = 0,
    @ServerTimestamp val timestamp: Timestamp? = null,
    @DocumentId var firestoreID: String = ""
)
