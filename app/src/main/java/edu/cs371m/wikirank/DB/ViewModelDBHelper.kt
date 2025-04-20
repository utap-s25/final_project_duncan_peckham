package edu.cs371m.wikirank.DB


import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.cs371m.wikirank.api.WikiShortArticle

class ViewModelDBHelper {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // If we want to listen for real time updates use this
    // .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
    // want to use this to listen to votes on current matchup


    //todo db design:
    // get a list of top 100 people (excluding some people), upload them to the db.
    // Then design a function to query that, use that to send a wiki request to get more info.
    // Have another collection for match-ups, which will have the two options and the winning option.
    // (Will need to pull this whole collection when calculating elo scores locally, unless I want to do gradual calculations online)
    // have another collection for a user's favorite articles
    private fun limitAndGet(query: Query,
                            resultListener: (List<DBArticle>)->Unit) {
        query
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "allNotes fetch ${result!!.documents.size}")
                // NB: This is done on a background thread
                resultListener(result.documents.mapNotNull {
                    it.toObject(DBArticle::class.java)
                })
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "allNotes fetch FAILED ", it)
                resultListener(listOf())
            }
    }

    fun fetchCategory(
        category: String,
        resultListener: (List<DBArticle>) -> Unit
    ){
        val ref = db.collection("articles")
        val query = ref.orderBy("order_id")
        limitAndGet(query, resultListener)
    }
}