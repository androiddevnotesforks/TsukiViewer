package com.flamyoad.tsukiviewer.ui.reader

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flamyoad.tsukiviewer.utils.ImageFileFilter
import java.io.File

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val imageList = MutableLiveData<List<File>>()

    fun imageList(): LiveData<List<File>> = imageList

    var totalImageCount: Int = -1
        private set


    var currentPath: String = ""

    fun scanForImages(dirPath: String) {
        if (dirPath == currentPath) {
            return
        }

        val dir = File(dirPath)
        val fetchedImages = dir.listFiles(ImageFileFilter()).sorted()

        imageList.value = fetchedImages

        totalImageCount = fetchedImages.size
    }

}