package com.flamyoad.tsukiviewer.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.flamyoad.tsukiviewer.R
import com.flamyoad.tsukiviewer.model.BookmarkItem
import com.flamyoad.tsukiviewer.model.Doujin
import com.flamyoad.tsukiviewer.ui.doujinpage.DoujinDetailsActivity
import com.flamyoad.tsukiviewer.ActionModeListener
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller

class BookmarkItemsAdapter(
    private val actionListener: ActionModeListener<BookmarkItem>,
    var actionModeEnabled: Boolean

) : ListAdapter<BookmarkItem, BookmarkItemsAdapter.BookmarkViewHolder>(BookmarkDiffCallback()),
    RecyclerViewFastScroller.OnPopupTextUpdate {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.doujin_list_item, parent, false)

        val coverImage: ImageView = view.findViewById(R.id.imgCover)

        val holder = BookmarkViewHolder(view)

        view.setOnClickListener {
            val itemIndex = holder.adapterPosition
            val item = getItem(itemIndex)

            when (actionModeEnabled) {
                true -> {
                    actionListener.onMultiSelectionClick(item)
                }

                false -> {
                    val position = holder.bindingAdapterPosition
                    if (position == RecyclerView.NO_POSITION) {
                        return@setOnClickListener
                    }

                    val currentDoujin = item.doujin
                    openDoujin(it.context, currentDoujin)
                }
            }
        }

        view.setOnLongClickListener {
            if (!actionModeEnabled) {
                val zoomIn = AnimationUtils.loadAnimation(it.context, R.anim.doujin_img_zoom_in)
                val zoomOut = AnimationUtils.loadAnimation(it.context, R.anim.doujin_img_zoom_out)

                coverImage.startAnimation(zoomIn)

                actionListener.startActionMode()

                coverImage.startAnimation(zoomOut)
            }

            val itemIndex = holder.adapterPosition
            val item = getItem(itemIndex)

            actionListener.onMultiSelectionClick(item)
            return@setOnLongClickListener true
        }

        return holder
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        Log.d("debuff", "onBindViewHolder called for position $position")
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id ?: -1
    }

    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coverImg: ImageView = itemView.findViewById(R.id.imgCover)
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitleEng)
        private val txtPageNumber: TextView = itemView.findViewById(R.id.txtPageNumber)
        private val multiSelectIndicator: ImageView =
            itemView.findViewById(R.id.multiSelectIndicator)

        fun bind(item: BookmarkItem) {
            val doujin = item.doujin
            if (doujin != null) {
                Glide.with(itemView.context)
                    .load(doujin.pic)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .sizeMultiplier(0.75f)
                    .into(coverImg)

                txtTitle.text = doujin.title
                txtPageNumber.text = doujin.numberOfItems.toString()
            }

            when (item.isSelected) {
                true -> setIconVisibility(View.VISIBLE)
                false -> setIconVisibility(View.GONE)
            }
        }

        private fun setIconVisibility(visibility: Int) {
            when (visibility) {
                View.VISIBLE -> multiSelectIndicator.visibility = visibility
                View.GONE -> multiSelectIndicator.visibility = visibility
                else -> return
            }
        }
    }

    private fun openDoujin(context: Context, doujin: Doujin?) {
        if (doujin == null) {
            return
        }

        val intent = Intent(context, DoujinDetailsActivity::class.java)

        intent.putExtra(LocalDoujinsAdapter.DOUJIN_FILE_PATH, doujin.path.toString())
        intent.putExtra(LocalDoujinsAdapter.DOUJIN_NAME, doujin.title)

        context.startActivity(intent)
    }

    override fun onChange(position: Int): CharSequence {
        return ""
    }
}

class BookmarkDiffCallback : DiffUtil.ItemCallback<BookmarkItem>() {
    override fun areItemsTheSame(oldItem: BookmarkItem, newItem: BookmarkItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BookmarkItem, newItem: BookmarkItem): Boolean {
        val status = oldItem == newItem
                && oldItem.isSelected == newItem.isSelected

        return status
    }
}
