package com.flamyoad.tsukiviewer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flamyoad.tsukiviewer.R
import com.flamyoad.tsukiviewer.model.IncludedPath
import com.flamyoad.tsukiviewer.ui.settings.includedfolders.RemovePathListener
import java.lang.IllegalArgumentException

class IncludedPathAdapter(private val listener: RemovePathListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val PATH_ITEM = 0
    private val EMPTY_INDICATOR = 1

    private var pathList = emptyList<IncludedPath>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            PATH_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.included_path_item, parent, false)

                val holder = PathViewHolder(view)

                val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)
                btnDelete.setOnClickListener {
                    val path = pathList[holder.adapterPosition]
                    listener.removePath(path)
                }

                return holder
            }

            EMPTY_INDICATOR -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.loading_indicator, parent, false)

                return EmptyViewHolder(view)
            }

            else ->  {
                throw IllegalArgumentException("Illegal view type")
            }
        }
    }

    override fun getItemCount(): Int {
        return if (pathList.isNotEmpty()) {
            pathList.size
        } else {
            1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (pathList.isNullOrEmpty()) {
            EMPTY_INDICATOR
        } else {
            PATH_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            PATH_ITEM -> {
                val pathHolder = holder as PathViewHolder

                val adapterPosition = pathHolder.adapterPosition

                pathHolder.bind(pathList[adapterPosition])
            }
        }
    }

    fun setList(list: List<IncludedPath>) {
        pathList = list
        notifyDataSetChanged()
    }

    inner class PathViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtDirectory: TextView = itemView.findViewById(R.id.txtDirectory)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: IncludedPath) {
            txtDirectory.text = item.dir.canonicalPath
        }
    }
}