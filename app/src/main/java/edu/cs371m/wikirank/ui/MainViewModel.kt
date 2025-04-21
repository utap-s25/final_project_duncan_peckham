package edu.cs371m.wikirank.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs371m.wikirank.DB.DBArticle
import edu.cs371m.wikirank.DB.MatchUp
import edu.cs371m.wikirank.User
import edu.cs371m.wikirank.DB.ViewModelDBHelper
import edu.cs371m.wikirank.DB.Vote
import edu.cs371m.wikirank.api.WikiApi
import edu.cs371m.wikirank.api.WikiArticle
import edu.cs371m.wikirank.api.WikiArticleRepository
import edu.cs371m.wikirank.api.WikiShortArticle
import edu.cs371m.wikirank.invalidUser
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private var currentAuthUser = invalidUser
    private val dbHelp = ViewModelDBHelper()

    private val refreshTrigger = MutableLiveData<Unit>()

    private val wikiApi: WikiApi = WikiApi.create()
    private val wikiApiRepository: WikiArticleRepository = WikiArticleRepository(wikiApi)

    private var leaderboards = MutableLiveData<Map<String, List<WikiShortArticle>>>()
    private var curCategory = MutableLiveData<String>("cities")

    private var articleOneIndex = MutableLiveData<Int>(0)
    private var articleTwoIndex = MutableLiveData<Int>(0)

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

    init {
        randomizeDBArticles()
    }

    private var articleOneShort = MediatorLiveData<WikiShortArticle>().apply{
        addSource(articleOneDB) { newArticle: DBArticle ->
            Log.d("articleOneShort", "Repo Short Article Request")
            viewModelScope.launch{
                this@apply.postValue(wikiApiRepository.getShortArticle(newArticle.name))
            }
        }
        addSource(refreshTrigger) {
            viewModelScope.launch{
                if(articleOneDB.value != null){
                    this@apply.postValue(wikiApiRepository.getShortArticle(articleOneDB.value!!.name))
                }
            }
        }
    }
    private var articleTwoShort = MediatorLiveData<WikiShortArticle>().apply{
        addSource(articleTwoDB) { newArticle: DBArticle ->
            Log.d("articleTwoShort", "Repo Short Article Request")
            viewModelScope.launch{
                this@apply.postValue(wikiApiRepository.getShortArticle(newArticle.name))
            }
        }
        addSource(refreshTrigger) {
            viewModelScope.launch{
                if(articleTwoDB.value != null){
                    this@apply.postValue(wikiApiRepository.getShortArticle(articleTwoDB.value!!.name))
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
            userId = currentAuthUser.uid,
            vote = vote
        )
        dbHelp.addVote(matchUp, successListener)
    }

    fun setCurrentAuthUser(user: User){
        currentAuthUser = user
    }

    fun isLoggedIn(): Boolean{
        return currentAuthUser != invalidUser
    }

    fun fetchCategory(category: String, resultListener:(List<DBArticle>) -> Unit){
        dbHelp.fetchCategory(category){
            Log.d("viewmodel", it.toString())
        }
    }

    fun addArticle(category: String, l: List<String>){
        dbHelp.addArticles(category, l)
    }

    fun getLeaderboardPos(category: String, position: Int): WikiShortArticle? {
        return leaderboards.value?.get(category)?.get(position)
    }

}