package edu.cs371m.wikirank.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs371m.wikirank.DB.DBArticle
import edu.cs371m.wikirank.User
import edu.cs371m.wikirank.DB.ViewModelDBHelper
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

    private var articleOneDB = MutableLiveData<DBArticle>()
    private var articleTwoDB = MutableLiveData<DBArticle>()


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

    suspend fun getArticle(title: String): WikiArticle{
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

    fun getDBArticles(){
        val rand1 = (2..101).random()
        val rand2 = (2..101).random()
        dbHelp.fetchArticle("cities", rand1){
            if(it.isNotEmpty()){
                articleOneDB.postValue(it[0])
            }

        }
        dbHelp.fetchArticle("cities", rand2){
            if(it.isNotEmpty()){
                articleTwoDB.postValue(it[0])
            }

        }
    }

    fun repoFetch(){
        Log.d("repoFetch", "Refreshing")
        refreshTrigger.value = Unit
    }

    fun repoVote(){
        // todo - upload current matchup vote --
    }

    fun getVote(){
        // todo - query votes for current matchup - to be run when there is an update not from the current user
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

}