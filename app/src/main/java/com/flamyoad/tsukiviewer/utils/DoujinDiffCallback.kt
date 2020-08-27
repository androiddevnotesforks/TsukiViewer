package com.flamyoad.tsukiviewer.utils

import androidx.recyclerview.widget.DiffUtil
import com.flamyoad.tsukiviewer.model.Doujin

class DoujinDiffCallback : DiffUtil.ItemCallback<Doujin>() {

    override fun areItemsTheSame(oldItem: Doujin, newItem: Doujin): Boolean {
        return oldItem.path == newItem.path
    }

    override fun areContentsTheSame(oldItem: Doujin, newItem: Doujin): Boolean {
        return oldItem == newItem
    }

}