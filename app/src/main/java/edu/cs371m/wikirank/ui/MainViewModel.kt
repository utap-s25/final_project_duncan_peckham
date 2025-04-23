package edu.cs371m.wikirank.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import edu.cs371m.wikirank.DB.DBArticle
import edu.cs371m.wikirank.DB.MatchUp
import edu.cs371m.wikirank.User
import edu.cs371m.wikirank.DB.ViewModelDBHelper
import edu.cs371m.wikirank.DB.Vote
import edu.cs371m.wikirank.api.WikiApi
import edu.cs371m.wikirank.api.WikiArticle
import edu.cs371m.wikirank.api.WikiArticleRepository
import edu.cs371m.wikirank.api.WikiShortArticle
import edu.cs371m.wikirank.api.WikiThumbnail
import edu.cs371m.wikirank.invalidUser
import edu.cs371m.wikirank.utility.RankedObject
import edu.cs371m.wikirank.utility.Ranker
import kotlinx.coroutines.launch
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import edu.cs371m.wikirank.utility.RatedArticle
import kotlinx.coroutines.Dispatchers


class MainViewModel: ViewModel() {
    private var _authUser = MutableLiveData<User>(invalidUser)
    val authUser: LiveData<User> get() = _authUser

    private val dbHelp = ViewModelDBHelper()

    private val refreshTrigger = MutableLiveData<Unit>()

    private val wikiApi: WikiApi = WikiApi.create()
    private val wikiApiRepository: WikiArticleRepository = WikiArticleRepository(wikiApi)

    private var leaderboards = MutableLiveData<Map<String, List<DBArticle>>>()
    private var curCategory = MutableLiveData<String>("cities")

    private var articleOneIndex = MutableLiveData<Int>(0)
    private var articleTwoIndex = MutableLiveData<Int>(0)

    private val idToDB = mutableMapOf<String, DBArticle>() // associate firestore id to DB articles (used for matchup -> WikiShortArticle)



    private var articleOneDB = MediatorLiveData<DBArticle>().apply{
        addSource(articleOneIndex) {newInd: Int ->
            dbHelp.fetchArticle("cities", newInd){
                if(it.isNotEmpty()){
                    this@apply.postValue(it[0])
                }
            }
        }
    }
    private var articleTwoDB =MediatorLiveData<DBArticle>().apply{
        addSource(articleTwoIndex) {newInd: Int ->
            dbHelp.fetchArticle("cities", newInd){
                if(it.isNotEmpty()){
                    this@apply.postValue(it[0])
                }
            }
        }
    }

    private var categoryList = MediatorLiveData<List<String>>().apply{
        addSource(leaderboards) {newLeaderboard ->
            val ids = newLeaderboard?.get(curCategory.value)?.map{it.firestoreID} ?: emptyList()
            postValue(ids)
        }
        addSource(curCategory){ newCategory ->
            val ids = leaderboards.value?.get(newCategory)?.map{it.firestoreID} ?: emptyList()
            postValue(ids)
        }
    }

    fun getLeaderboards(){
        Log.d(javaClass.simpleName, "getLeaderboards")
        dbHelp.fetchRawLeaderboard { rawLeaderboard ->
            leaderboards.postValue(
                rawLeaderboard.mapValues { dbArticleList ->
                    dbArticleList.value.map{dbArticle ->
                        idToDB[dbArticle.firestoreID] = dbArticle
                        return@map dbArticle
                    }
                }
            )
        }
    }

    private val matchups: MediatorLiveData<List<MatchUp>> = MediatorLiveData<List<MatchUp>>().apply {
        addSource(curCategory){newCategory ->
            startListeningToMatchups(newCategory)
        }
    }

    private var matchupListener: ListenerRegistration? = null
    fun startListeningToMatchups(category: String){
        matchupListener?.remove()

        matchupListener = dbHelp.listenMatchups(category){ newList ->
            matchups.postValue(newList)
        }
    }

    override fun onCleared() {
        super.onCleared()
        matchupListener?.remove()
    }

    private var ranker = Ranker(emptyList(), emptyList())
    private val _rankedObjects = MutableLiveData<List<RankedObject>>()
    private val rankedObjects: LiveData<List<RankedObject>> = _rankedObjects

    val ratedArticles = MediatorLiveData<List<RatedArticle>>().apply {
        fun rebuild() {
            val ros = _rankedObjects.value ?: return
            if (ros.isEmpty()) { postValue(emptyList()); return }

            viewModelScope.launch {
                val keys = ros.mapNotNull { idToDB[it.id]?.name }
                if (keys.isEmpty()) { postValue(emptyList()); return@launch }

                val shortMap = wikiApiRepository.getShortArticlesMap(keys)

                val rows = ros.mapNotNull { ro ->
                    val db   = idToDB[ro.id]                ?: return@mapNotNull null
                    val wiki = shortMap[db.name] ?: wikiApiRepository.getShortArticleCached(db.name) ?: return@mapNotNull null
                    RatedArticle(wiki, ro.rating, ro.id)
                }
                postValue(rows)
            }
        }
        /* Recompute when either the Elo ranking OR the id-lookup changes */
        addSource(_rankedObjects) { rebuild() }     // ratings / match-ups changed
        addSource(leaderboards)    { rebuild() }    // idToDB was just updated
    }


    private var articleOneShort = MediatorLiveData<WikiShortArticle>().apply{
        addSource(articleOneDB) { newArticle: DBArticle ->
            Log.d("articleOneShort", "Repo Short Article Request")
            viewModelScope.launch{
                this@apply.postValue(wikiApiRepository.getShortArticleCached(newArticle.name))
            }
        }
        addSource(refreshTrigger) {
            viewModelScope.launch{
                if(articleOneDB.value != null){
                    this@apply.postValue(wikiApiRepository.getShortArticleCached(articleOneDB.value!!.name))
                }
            }
        }
    }
    private var articleTwoShort = MediatorLiveData<WikiShortArticle>().apply{
        addSource(articleTwoDB) { newArticle: DBArticle ->
            Log.d("articleTwoShort", "Repo Short Article Request")
            viewModelScope.launch{
                this@apply.postValue(wikiApiRepository.getShortArticleCached(newArticle.name))
            }
        }
        addSource(refreshTrigger) {
            viewModelScope.launch{
                if(articleTwoDB.value != null){
                    this@apply.postValue(wikiApiRepository.getShortArticleCached(articleTwoDB.value!!.name))
                }
            }
        }
    }

    init {
        articleOneShort.observeForever { value ->
            Log.d("DebugObserver", "articleOneShort changed: $value")
        }
        articleTwoShort.observeForever { value ->
            Log.d("DebugObserver", "articleTwoShort changed: $value")
        }
    }

    suspend fun getArticle(title: String): WikiArticle? {
        // hit cache
        return wikiApiRepository.getArticle(title)
    }

    fun getArticleOne(): WikiShortArticle? {
        return articleOneShort.value
    }
    fun getArticleTwo(): WikiShortArticle? {
        return articleTwoShort.value
    }

    fun observeArticleOne(): LiveData<WikiShortArticle>{
        return articleOneShort
    }

    fun observeArticleTwo(): LiveData<WikiShortArticle>{
        return articleTwoShort
    }

    fun randomizeDBArticles(){
        val rand1 = (2..101).random()
        val rand2 = (2..101).random()
        articleOneIndex.postValue(rand1)
        articleTwoIndex.postValue(rand2)
    }

    fun repoFetch(){
        Log.d("repoFetch", "Refreshing")
        refreshTrigger.value = Unit
    }

    fun castVote(vote: Vote, successListener: () -> Unit){
        val matchUp = MatchUp(
            articleOne = articleOneDB.value?.firestoreID.toString(),
            articleTwo = articleTwoDB.value?.firestoreID.toString(),
            category = articleOneDB.value?.category ?: return,
            userId = authUser.value?.uid ?: "",
            vote = vote
        )
        dbHelp.addVote(matchUp, successListener)
    }

    fun setCurrentAuthUser(user: User){
        _authUser.value = user
    }

    fun isLoggedIn(): Boolean{
        return authUser.value != invalidUser
    }

    fun fetchCategory(category: String, resultListener:(List<DBArticle>) -> Unit){
        dbHelp.fetchCategory(category){
            Log.d("viewmodel", it.toString())
        }
    }


    fun addArticle(category: String, l: List<String>){
        dbHelp.addArticles(category, l)
    }

    fun getThumbnail(title: String, size: Int, onSuccess: (WikiThumbnail) -> Unit){
        viewModelScope.launch{
            val thumbnail = wikiApiRepository.getThumbnail(title, size)
            if(thumbnail != null){
                onSuccess(thumbnail)
            }
        }
    }

    //ChatGPT Created the base code given a prompt containing UserMatchUpDisplay and wanting to filter matchup list to the users id
    val userMatchupRows = MediatorLiveData<List<UserMatchupDisplay>>().apply {
        fun rebuild() {
            val uid   = _authUser.value?.uid ?: return
            val votes = matchups.value ?: return
            val myVotes = votes.filter { it.userId == uid }

            /* ---------- ensure idToDB has every article ID we need ---------- */
            val missing = myVotes.flatMap { listOf(it.articleOne, it.articleTwo) }
                .filter { it !in idToDB }
            if (missing.isNotEmpty()) {
                dbHelp.fetchArticlesByIds(missing) { fetched ->
                    fetched.forEach { idToDB[it.firestoreID] = it }
                    rebuild()                      // retry once map is full
                }
                return
            }

            /* ---------- fetch Wiki shorts ---------- */
            val titles  = myVotes.flatMap {
                listOf(idToDB[it.articleOne]!!.name, idToDB[it.articleTwo]!!.name)
            }.distinct()

            viewModelScope.launch(Dispatchers.IO) {
                val shortMap = wikiApiRepository.getShortArticlesMap(titles)

                val rows = myVotes.mapNotNull { mu ->
                    val left  = shortMap[idToDB[mu.articleOne]!!.name]
                        ?: wikiApiRepository.getShortArticleCached(idToDB[mu.articleOne]!!.name)
                    val right = shortMap[idToDB[mu.articleTwo]!!.name]
                        ?: wikiApiRepository.getShortArticleCached(idToDB[mu.articleTwo]!!.name)

                    if (left == null || right == null){
                        Log.d("userMatchupRows", "Failed in fetching ${idToDB[mu.articleOne]?.name} or ${idToDB[mu.articleTwo]?.name}")
                        return@mapNotNull null
                    }
                    val score = if (mu.vote == Vote.ARTICLE_ONE) "1-0" else "0-1"
                    UserMatchupDisplay(left, right, score)
                }
                postValue(rows)
            }
        }


        addSource(matchups) { rebuild() }    // Firestore update
        addSource(_authUser) { rebuild() }   // login / logout
    }


    init {
        randomizeDBArticles()
        getLeaderboards()

        categoryList.observeForever {
            ranker.resetArticles(it)
            _rankedObjects.value = ranker.getRankedObjects()
        }
        matchups.observeForever {
            ranker.addMatchups(it)
            _rankedObjects.value = ranker.getRankedObjects()
        }
    }
}