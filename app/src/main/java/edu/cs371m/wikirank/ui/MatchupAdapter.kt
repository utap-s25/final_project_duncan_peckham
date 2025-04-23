package edu.cs371m.wikirank.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.cs371m.wikirank.api.WikiShortArticle
import edu.cs371m.wikirank.databinding.ArticleRowBinding
import edu.cs371m.wikirank.databinding.MatchupRowBinding
import edu.cs371m.wikirank.ui.ArticleAdapter.VH

class MatchupAdapter(
    private val navigate: (WikiShortArticle) -> Unit       // callback from Fragment
) : ListAdapter<UserMatchupDisplay, MatchupAdapter.VH>(Diff()) {

    class Diff : DiffUtil.ItemCallback<UserMatchupDisplay>() {
        override fun areItemsTheSame(oldItem: UserMatchupDisplay, newItem: UserMatchupDisplay): Boolean{
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItem: UserMatchupDisplay, newItem: UserMatchupDisplay): Boolean {
            return oldItem == newItem
        }
    }

    inner class VH(private val b: MatchupRowBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(position: Int) {
            b.leftTitle.text = getItem(position).left.title
            b.rightTitle.text = getItem(position).right.title
            b.score.text = getItem(position).score

            b.leftTitle.setOnClickListener  { navigate(getItem(position).left)  }
            b.rightTitle.setOnClickListener { navigate(getItem(position).right)  }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val rowBinding = MatchupRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return VH(rowBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(position)
    }
}
