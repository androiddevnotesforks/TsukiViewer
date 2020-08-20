package com.flamyoad.tsukiviewer.ui.doujinpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.flamyoad.tsukiviewer.R
import com.flamyoad.tsukiviewer.adapter.DoujinTagsAdapter
import com.flamyoad.tsukiviewer.adapter.LocalDoujinsAdapter
import com.flamyoad.tsukiviewer.model.DoujinDetailsWithTags
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxItemDecoration
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_doujin_details.*
import kotlinx.android.synthetic.main.doujin_details_tags_group.*
import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class FragmentDoujinDetails : Fragment() {

    private val viewmodel by activityViewModels<DoujinViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_doujin_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewmodel.coverImage.observe(viewLifecycleOwner, Observer { image ->
            Glide.with(this)
                .load(image)
                .sizeMultiplier(0.75f)
                .into(imgBackground)

            Glide.with(this)
                .load(image)
                .into(imgCover)
        })

        viewmodel.detailWithTags.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                setDefaultToolbarText()
            } else {
                initDoujinDetails(it)
            }
        })
    }

    // Show directory name if metadata not yet obtained from API
    private fun setDefaultToolbarText() {
        val currentPath = requireActivity()
            .intent
            .getStringExtra(LocalDoujinsAdapter.DOUJIN_FILE_PATH)

        val dir = File(currentPath)
        txtTitleEng.text = dir.name
    }

    private fun initDoujinDetails(item: DoujinDetailsWithTags) {
        txtTitleEng.text = item.doujinDetails.fullTitleEnglish

        txtTitleJap.text = item.doujinDetails.fullTitleJapanese

        val parodies = item.tags.filter { x -> x.type == "parody" }
        val chars = item.tags.filter { x -> x.type == "character" }
        val tags = item.tags.filter { x -> x.type == "tag" }
        val artists = item.tags.filter { x -> x.type == "artist" }
        val groups = item.tags.filter { x -> x.type == "group" }
        val langs = item.tags.filter { x -> x.type == "language" }
        val categories = item.tags.filter { x -> x.type == "category" }

        val listofTagGroups = listOf(
            parodies, chars, tags, artists, groups, langs, categories
        )

        for (i in listofTagGroups.indices) {
            val group = listofTagGroups[i]

            val adapter = DoujinTagsAdapter()
            adapter.setList(group)

            val flexLayoutManager = FlexboxLayoutManager(context, FlexDirection.ROW)
            flexLayoutManager.apply {
                flexWrap = FlexWrap.WRAP
            }

            val itemDecoration = FlexboxItemDecoration(context)
            itemDecoration.setOrientation(FlexboxItemDecoration.BOTH)

            val recyclerView = when (i) {
                0 -> listParodies
                1 -> listCharacters
                2 -> listTags
                3 -> listArtists
                4 -> listGroups
                5 -> listLanguages
                6 -> listCategories
                else -> null
            }

            recyclerView?.adapter = adapter
            recyclerView?.layoutManager = flexLayoutManager

            // Disable scrolling of the recyclerviews
            recyclerView?.suppressLayout(true)

            recyclerView?.addItemDecoration(itemDecoration)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): FragmentDoujinDetails {
            return FragmentDoujinDetails()
        }
    }
}

