package edu.cs371m.wikirank.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs371m.wikirank.api.WikiApi
import edu.cs371m.wikirank.api.WikiArticleRepository
import edu.cs371m.wikirank.api.WikiShortArticle
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    // potentially have title for each article, then the short/long articles as mediator live data based on that
    private val refreshTrigger = MutableLiveData<Unit>()

    private val wikiApi: WikiApi = WikiApi.create()
    private val wikiApiRepository: WikiArticleRepository = WikiArticleRepository(wikiApi)
    private var articleOneTitle = MutableLiveData<String>().apply{
        value = "Boston"
    }
    private var articleTwoTitle = MutableLiveData<String>().apply{
        value = "New York City"
    }

    private var articleOneShort = MediatorLiveData<WikiShortArticle>().apply{
        addSource(articleOneTitle) { title: String ->
            Log.d("articleOneShort", "Repo Short Article Request")
            viewModelScope.launch{
                this@apply.postValue(wikiApiRepository.getShortArticle(title))
            }
        }
        addSource(refreshTrigger) {
            viewModelScope.launch{
                this@apply.postValue(wikiApiRepository.getShortArticle(articleOneTitle.value!!))
            }
        }
    }
    private var articleTwoShort = MediatorLiveData<WikiShortArticle>().apply{
        addSource(articleTwoTitle) { title: String ->
            Log.d("articleTwoShort", "Repo Short Article Request")
            viewModelScope.launch{
                this@apply.postValue(wikiApiRepository.getShortArticle(title))
            }
        }
        addSource(refreshTrigger) {
            viewModelScope.launch{
                this@apply.postValue(wikiApiRepository.getShortArticle(articleTwoTitle.value!!))
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

    fun setArticleOneTitle(newTitle: String){
        articleOneTitle.value= newTitle
    }

    fun setArticleTwoTitle(newTitle: String){
        articleTwoTitle.value= newTitle
    }

    fun repoFetch(){
        Log.d("repoFetch", "Refreshing")
        refreshTrigger.value = Unit
    }


}