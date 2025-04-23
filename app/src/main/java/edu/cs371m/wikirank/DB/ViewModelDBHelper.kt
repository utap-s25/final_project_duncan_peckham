package edu.cs371m.wikirank.DB


import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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
    private fun <T> limitAndGet(query: Query,
                            clazz: Class<T>,
                            resultListener: (List<T>)->Unit) {
        query
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "allNotes fetch ${result!!.documents.size}")
                // NB: This is done on a background thread
                resultListener(result.documents.mapNotNull {
                    it.toObject(clazz)
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
        val query = ref
            .whereEqualTo("category", category)
            .orderBy("order_id")
        limitAndGet(query, DBArticle::class.java, resultListener)
    }

    fun fetchArticle(
        category: String,
        orderRank: Int,
        resultListener: (List<DBArticle>) -> Unit){
        val ref = db.collection("articles")
        val query = ref
            .whereEqualTo("category", category)
            .whereEqualTo("order_id", orderRank)
            .orderBy("order_id")
            .limit(1)
        limitAndGet(query, DBArticle::class.java, resultListener)
    }


    // I gave chatGPT the rest of this file and the function signature and had it write this function
    fun fetchRawLeaderboard(
        resultListener: (Map<String, List<DBArticle>>) -> Unit
    ) {
        Log.d(javaClass.simpleName, "fetchRawLeaderboard")
        val ref = db.collection("articles")

        // Pull every article once, then group in-memory
        limitAndGet(ref, DBArticle::class.java) { allArticles ->

            // Group by category and make sure each list is ordered
            val leaderboard: Map<String, List<DBArticle>> =
                allArticles
                    .groupBy { it.category }
                    .mapValues { (_, list) -> list.sortedBy { it.order_id } }
            resultListener(leaderboard)
        }
    }


    fun fetchMatchups(
        category: String,
        resultListener: (List<MatchUp>) -> Unit
    ){
        val ref = db.collection("matchups")
        val query = ref
            .whereEqualTo("category", category)
            .orderBy("timestamp")
        limitAndGet(query, MatchUp::class.java, resultListener)
    }

    fun listenMatchups(
        category: String,
        onChange: (List<MatchUp>) -> Unit
    ): ListenerRegistration{
        val query = db.collection("matchups")
            .whereEqualTo("category", category)
            .orderBy("timestamp")

        return query.addSnapshotListener{ snap, err ->
            if (err != null) {
                Log.d(javaClass.simpleName, "Snapshot failed with error $err")
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull{it.toObject(MatchUp::class.java)}

            onChange(list ?: emptyList())
        }
    }

    fun addVote(matchUp: MatchUp, onSuccess: () -> Unit){
        val ref = db.collection("matchups")
        ref.add(matchUp).addOnSuccessListener {
            Log.d("addVote", "successfully added vote")
            onSuccess()
        }
            .addOnFailureListener {
                Log.d("addVote", "failed adding vote")
            }
    }

    fun addArticles(
        category: String,
        names: List<String>
    ){
        val ref = db.collection("articles")
        // Step 1: Find the highest order_id in the given category
        ref.whereEqualTo("category", category)
            .orderBy("order_id", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val maxOrderId = result.documents.firstOrNull()
                    ?.getLong("order_id") ?: 0
                var newOrderId = maxOrderId + 1L
                for(name in names){
                    val newArticle = DBArticle(
                        name = name,
                        category = category,
                        order_id = newOrderId.toInt()
                    )

                    // Add to the collection
                    ref.add(newArticle)
                        .addOnSuccessListener {
                            Log.d("addArticle", "Successfully added article with order_id $newOrderId")
                        }
                        .addOnFailureListener {
                            Log.e("addArticle", "Failed to add article", it)
                        }
                    newOrderId++
                }

            }
            .addOnFailureListener {
                Log.e("addArticle", "Failed to fetch max order_id", it)
            }
    }
}