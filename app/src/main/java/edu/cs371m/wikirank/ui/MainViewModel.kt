package edu.cs371m.wikirank.ui

import android.util.Log
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
    private val wikiApi: WikiApi = WikiApi.create()
    private val wikiApiRepository: WikiArticleRepository = WikiArticleRepository(wikiApi)
    private var articleOneTitle: MutableLiveData<String> = MutableLiveData()
    private var articleTwoTitle: MutableLiveData<String> = MutableLiveData()

    private var articleOneShort: MutableLiveData<WikiShortArticle> = MediatorLiveData<WikiShortArticle>().apply{
        addSource(articleOneTitle) { title: String ->
            Log.d("articleOneShort", "Repo Short Article Request")
            viewModelScope.launch{
                this@apply.postValue(wikiApiRepository.getShortArticle(title))
            }
        }
    }
    private var articleTwoShort: MutableLiveData<WikiShortArticle> = MediatorLiveData<WikiShortArticle>().apply{
        addSource(articleTwoTitle) { title: String ->
            Log.d("articleOneShort", "Repo Short Article Request")
            viewModelScope.launch{
                this@apply.postValue(wikiApiRepository.getShortArticle(title))
            }
        }
    }

    fun getArticleOne(): WikiShortArticle? {
        return articleOneShort.value
    }
    fun getArticleTwo(): WikiShortArticle? {
        return articleTwoShort.value
    }

    fun setArticleOneTitle(newTitle: String){
        articleOneTitle.value= newTitle
    }

    fun setArticleTwoTitle(newTitle: String){
        articleTwoTitle.value= newTitle
    }


}