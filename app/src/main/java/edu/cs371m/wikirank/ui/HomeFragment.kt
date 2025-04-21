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
//        val cities = context?.resources?.getStringArray(R.array.city)?.toList()
//        if (cities != null) {
//            viewModel.addArticle("cities", cities)
//        }
        viewModel.getDBArticles()

        viewModel.observeArticleOne().observe(viewLifecycleOwner){
            binding.articleTitle1.text = it.title
            binding.articleDescription1.text = it.shortDescription
            Glide.glideFetch(it.getImageUrl(), binding.articleImage1)
        }
        viewModel.observeArticleTwo().observe(viewLifecycleOwner){
            binding.articleTitle2.text = it.title
            binding.articleDescription2.text = it.shortDescription
            Glide.glideFetch(it.getImageUrl(), binding.articleImage2)
        }

        binding.articleTitle1.setOnClickListener {
            viewModel.fetchCategory("place"){}
            val nav = findNavController()
            val action = HomeFragmentDirections.actionHomeFragmentToArticle(viewModel.getArticleOne()!!)
            nav.navigate(action)
        }
        binding.articleTitle2.setOnClickListener {
            val nav = findNavController()
            val action = HomeFragmentDirections.actionHomeFragmentToArticle(viewModel.getArticleTwo()!!)
            nav.navigate(action)
        }
        // TODO Listeners on buttons to make votes
    }
}