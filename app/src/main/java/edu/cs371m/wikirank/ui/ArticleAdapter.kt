package edu.cs371m.wikirank.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import edu.cs371m.wikirank.api.WikiShortArticle
import edu.cs371m.wikirank.databinding.ArticleRowBinding
import edu.cs371m.wikirank.glide.Glide
import edu.cs371m.wikirank.utility.RatedArticle
import kotlin.math.round

// adapted from PhotoMetaAdapter from the PhotoList FC
class ArticleAdapter(private val viewModel: MainViewModel,
    private val navigateToArticle: (WikiShortArticle) -> Unit
    ): ListAdapter<RatedArticle, ArticleAdapter.VH>(Diff()) {
    class Diff : DiffUtil.ItemCallback<RatedArticle>() {
        override fun areItemsTheSame(oldItem: RatedArticle, newItem: RatedArticle): Boolean {
            return oldItem.article.title == newItem.article.title
        }

        override fun areContentsTheSame(oldItem: RatedArticle, newItem: RatedArticle): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
    inner class VH(private val rowBinding: ArticleRowBinding) :
        RecyclerView.ViewHolder(rowBinding.root) {

        fun bind(position: Int) {
            val shortArticle = getItem(position)
            if(shortArticle?.article != null){
                viewModel.getThumbnail(shortArticle.article.title, 80){ thumbnail ->
                    Glide.glideFetch(thumbnail.imageUrl, rowBinding.articleThumbnail)
                }
            }
            rowBinding.articleTitle.text = shortArticle?.article?.title
            rowBinding.articleDesc.text = shortArticle?.article?.shortDescription
            rowBinding.rankTV.text = (position + 1).toString()
            rowBinding.articleTitle.setOnClickListener{navigateToArticle(shortArticle.article)}
            if(shortArticle != null){
                Log.d("ArticleAdapter", "got shortArticle $shortArticle with rating ${shortArticle.rating}")
                rowBinding.ratingTV.text = round(shortArticle.rating).toString()
            } else{
                rowBinding.ratingTV.text = "1500"
            }

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