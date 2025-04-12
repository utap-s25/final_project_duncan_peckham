package edu.cs371m.wikirank.ui
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import edu.cs371m.wikirank.databinding.WikiVoteFragBinding
import edu.cs371m.wikirank.R
import edu.cs371m.wikirank.api.WikiShortArticle
import edu.cs371m.wikirank.glide.AppGlideModule
import edu.cs371m.wikirank.glide.Glide

class HomeFragment: Fragment(R.layout.wiki_vote_frag) {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = WikiVoteFragBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated")
        var articleOne: WikiShortArticle? = viewModel.getArticleOne()
        var articleTwo: WikiShortArticle? = viewModel.getArticleTwo()

        if(articleOne != null){
            binding.articleTitle1.text = articleOne.title
            binding.articleDescription1.text = articleOne.shortDescription
            //get image
        }
        if(articleTwo != null){
            binding.articleTitle2.text = articleTwo.title
            binding.articleDescription2.text = articleTwo.shortDescription
        }


        Glide.glideFetch("https://upload.wikimedia.org/wikipedia/commons/e/e2/Boston_-Massachusetts_State_House_%2848718911666%29.jpg", binding.articleImage1)
        Glide.glideFetch("https://upload.wikimedia.org/wikipedia/commons/4/45/Liberty02.jpg", binding.articleImage2)

    }
}