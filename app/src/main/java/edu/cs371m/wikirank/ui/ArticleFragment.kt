package edu.cs371m.wikirank.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import edu.cs371m.wikirank.glide.Glide
import edu.cs371m.wikirank.R
import edu.cs371m.wikirank.databinding.ArticleFragBinding
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class ArticleFragment: Fragment(R.layout.article_frag) {
    private val viewModel: MainViewModel by activityViewModels()
    private val args: ArticleFragmentArgs by navArgs()
    private var _binding: ArticleFragBinding? = null

    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ArticleFragBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated")

        Glide.glideFetch(args.article.getImageUrl(), binding.articleImage)
        binding.articleTitle.text = args.article.title
        binding.articleShortDescription.text = args.article.shortDescription
        // for full description, need to send new API request
        // todo refactor this to use viewmodel livedata instead
        viewLifecycleOwner.lifecycleScope.launch {
            binding.articleFullDescription.text = viewModel.getArticle(args.article.title)?.articleExtract
        }

    }
}