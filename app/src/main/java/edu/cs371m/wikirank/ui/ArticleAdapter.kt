package edu.cs371m.wikirank.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import edu.cs371m.wikirank.api.WikiShortArticle
import edu.cs371m.wikirank.databinding.ArticleRowBinding
import edu.cs371m.wikirank.glide.Glide

// adapted from PhotoMetaAdapter from the PhotoList FC
class ArticleAdapter(private val viewModel: MainViewModel,
    private val navigateToArticle: (WikiShortArticle) -> Unit
    ): ListAdapter<WikiShortArticle, ArticleAdapter.VH>(Diff()) {
    class Diff : DiffUtil.ItemCallback<WikiShortArticle>() {
        override fun areItemsTheSame(oldItem: WikiShortArticle, newItem: WikiShortArticle): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: WikiShortArticle, newItem: WikiShortArticle): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    inner class VH(private val rowBinding: ArticleRowBinding) :
        RecyclerView.ViewHolder(rowBinding.root) {

        fun bind(position: Int) {
            val shortArticle = getItem(position)
            if(shortArticle?.title != null){
                viewModel.getThumbnail(shortArticle.title){ thumbnail ->
                    Glide.glideFetch(thumbnail.imageUrl, rowBinding.articleThumbnail)
                }
            }
            rowBinding.articleTitle.text = shortArticle?.title
            rowBinding.articleDesc.text = shortArticle?.shortDescription

            rowBinding.articleTitle.setOnClickListener{navigateToArticle(shortArticle)}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val rowBinding = ArticleRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return VH(rowBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(position)
    }

}