package com.flamyoad.tsukiviewer.ui.reader

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.io.File

class ImageFragmentAdapter(activity: AppCompatActivity)
    : FragmentStateAdapter(activity) {

    private var imageList: List<File> = emptyList()

    fun setList(imageList: List<File>) {
        this.imageList = imageList
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun createFragment(position: Int): Fragment {
        val image = imageList[position]
        return ImageFragment.newInstance(image)
    }
}