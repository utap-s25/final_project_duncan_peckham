package edu.cs371m.wikirank.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cs371m.wikirank.R
import edu.cs371m.wikirank.databinding.LeaderboardFragBinding

class LeaderboardFragment: Fragment(R.layout.leaderboard_frag) {
    private val viewModel: MainViewModel by activityViewModels()

    private fun initAdapter(binding: LeaderboardFragBinding): ArticleAdapter{
        val articleAdapter = ArticleAdapter(viewModel){
            val action  =  LeaderboardFragmentDirections.actionLeaderboardFragToArticle(it)
            findNavController().navigate(action)
        }
        viewModel.observeCategoryList().observe(viewLifecycleOwner){ titleList ->
            viewModel.getShortArticles(titleList){ articleList ->
                articleAdapter.submitList(articleList)
            }
        }
        return articleAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        val binding = LeaderboardFragBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated")
        val adapter = initAdapter(binding)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.recyclerView.context)
    }
}