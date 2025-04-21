package edu.cs371m.wikirank.ui

import androidx.recyclerview.widget.RecyclerView
import com.google.protobuf.Internal.ListAdapter
import edu.cs371m.wikirank.api.WikiShortArticle
import edu.cs371m.wikirank.databinding.ArticleRowBinding
import edu.cs371m.wikirank.glide.Glide

// adapted from PhotoMetaAdapter from the PhotoList FC
class ArticleAdapter(private val viewModel: MainViewModel): ListAdapter<WikiShortArticle, ArticleAdapter.VH>() {

    inner class VH(private val rowBinding: ArticleRowBinding) :
        RecyclerView.ViewHolder(rowBinding.root) {

        fun bind(holder: VH, position: Int) {
            val thumbnail = getThumbnail()
            Glide.glideFetch(photoMeta.uuid, rowBinding.articleThumbnail)
            holder.rowBinding.rowPictureTitle.text = photoMeta.pictureTitle
            holder.rowBinding.rowSize.text = photoMeta.byteSize.toString()
        }
    }
}