package com.flamyoad.tsukiviewer.ui.doujinpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.flamyoad.tsukiviewer.R
import com.flamyoad.tsukiviewer.adapter.DoujinTagsAdapter
import com.flamyoad.tsukiviewer.adapter.LocalDoujinsAdapter
import com.flamyoad.tsukiviewer.model.DoujinDetailsWithTags
import com.flamyoad.tsukiviewer.ui.home.local.TransitionAnimationListener
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxItemDecoration
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.doujin_details_tags_group.*
import kotlinx.android.synthetic.main.fragment_doujin_details.*
import java.io.File
import java.util.*

class FragmentDoujinDetails : Fragment(), TransitionAnimationListener {
    private val COLLECTION_DIALOG_TAG = "collection_dialog"

    private val viewModel by activityViewModels<DoujinViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_doujin_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUi()
    }

    private fun initUi() {
        viewModel.coverImage().observe(viewLifecycleOwner, Observer { image ->
            Glide.with(this)
                .load(image)
                .sizeMultiplier(0.75f)
                .into(imgBackground)

            Glide.with(this)
                .load(image)
                .into(imgCover)
        })

        viewModel.detailWithTags.observe(viewLifecycleOwner, Observer {
            val currentPath = requireActivity()
                .intent
                .getStringExtra(LocalDoujinsAdapter.DOUJIN_FILE_PATH)

            val dir = File(currentPath)

            txtDirectory.text = dir.absolutePath
            txtDateModified.text = Date(dir.lastModified()).toString()

            if (it == null) {
                setDefaultToolbarText(dir)
                tagsNotFoundIndicator.visibility = View.VISIBLE
            } else {
                tagGroup.visibility = View.VISIBLE
                initDoujinDetails(it)
            }
        })

        viewModel.imageList().observe(viewLifecycleOwner, Observer {
            txtImageCount.text = it.size.toString()
        })

        fab.setOnClickListener {
            openCollectionDialog()
        }
    }

    // Show directory name if metadata not yet obtained from API
    private fun setDefaultToolbarText(dir: File) {
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

            val adapter = DoujinTagsAdapter(useLargerView = false)
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

    private fun openCollectionDialog() {
        val dialog = CollectionListDialog()
        dialog.show(childFragmentManager, COLLECTION_DIALOG_TAG)
    }

    override fun makeSceneTransitionAnimation(view: View): ActivityOptionsCompat {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            view,
            ViewCompat.getTransitionName(view) ?: ""
        )
        return options
    }

    companion object {
        @JvmStatic
        fun newInstance(): FragmentDoujinDetails {
            return FragmentDoujinDetails()
        }
    }
}

