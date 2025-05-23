package edu.cs371m.wikirank.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cs371m.wikirank.MainActivity
import edu.cs371m.wikirank.R
import edu.cs371m.wikirank.api.WikiShortArticle
import edu.cs371m.wikirank.api.WikiThumbnail
import edu.cs371m.wikirank.databinding.ProfileFragBinding

data class UserMatchupDisplay(
    val left: WikiShortArticle,
    val right: WikiShortArticle,
    val score: String
)

class ProfileFragment: Fragment(R.layout.profile_frag) {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ProfileFragBinding.bind(view)
        Log.d(javaClass.simpleName, "onViewCreated")


        binding.logOutBut.setOnClickListener {
            (requireActivity() as? MainActivity)?.logout()
        }

        (requireActivity() as? MainActivity)?.authUser?.observeUser()?.observe(viewLifecycleOwner){
            binding.profileEmail.text = it.email
            binding.profileUsername.text = it.name
        }


        val adapter = MatchupAdapter{ shortArticle ->
            val action = ProfileFragmentDirections.actionProfileFragmentToArticle(shortArticle)
            findNavController().navigate(action)
        }
        binding.matchupsRV.adapter = adapter
        binding.matchupsRV.layoutManager = LinearLayoutManager(context)

        viewModel.userMatchupRows.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }
    }
}