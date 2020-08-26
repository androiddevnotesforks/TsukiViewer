package com.flamyoad.tsukiviewer.ui.search

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.flamyoad.tsukiviewer.db.AppDatabase
import com.flamyoad.tsukiviewer.db.dao.DoujinDetailsDao
import com.flamyoad.tsukiviewer.db.dao.DoujinTagsDao
import com.flamyoad.tsukiviewer.db.dao.IncludedPathDao
import com.flamyoad.tsukiviewer.db.dao.TagDao
import com.flamyoad.tsukiviewer.model.Doujin
import com.flamyoad.tsukiviewer.model.IncludedPath
import com.flamyoad.tsukiviewer.utils.ImageFileFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class SearchResultViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase

    val folderDao: IncludedPathDao
    val doujinDetailsDao: DoujinDetailsDao
    val tagDao: TagDao
    val doujinTagDao: DoujinTagsDao

//    val includedFolderList: LiveData<List<IncludedFolder>>

    private val searchedResult = MutableLiveData<List<Doujin>>()

    fun searchedResult(): LiveData<List<Doujin>> = searchedResult

    init {
        db = AppDatabase.getInstance(application)

        folderDao = db.includedPathDao()
        doujinDetailsDao = db.doujinDetailsDao()
        tagDao = db.tagsDao()
        doujinTagDao = db.doujinTagDao()

//        includedFolderList = folderDao.getAll()
    }

    fun submitQuery(title: String, tags: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val folderFromDb = findFoldersFromDatabase(title, tags)

                /* Do not search from directories if . . .
                     1. The title query is blank
                     2. The user has requested to search from genre tags
                */
                val folderFromExplorer = if (title.isBlank() || tags.isNotBlank()) {
                    emptyList()
                } else {
                    findFoldersFromFileExplorer(title)
                }

                val uniqueFolders = mutableSetOf<String>()

                with(uniqueFolders) {
                    addAll(folderFromDb)
                    addAll(folderFromExplorer)
                }

                val searchResult = uniqueFolders.mapNotNull { folder ->
                    convertFolderPathToDoujin(folder)
                }

                this@SearchResultViewModel.searchedResult.postValue(searchResult)
            }
        }
    }

    private suspend fun findFoldersFromDatabase(title: String, tags: String): List<String> {
        var folderList = emptyList<String>()

        // search with both title and tags
        if (title.isNotBlank() && tags.isNotBlank()) {
            val tagList = tags.split(",")
                .map { tagName -> tagName }

            val doujinDetailItems = doujinDetailsDao.findByTags(tagList, tagList.size)
            folderList = doujinDetailItems
                .filter { item ->
                    item.fullTitleEnglish.toLowerCase(Locale.ROOT).contains(title)
                            || item.fullTitleJapanese.contains(title)
                }
                .map { item -> item.absolutePath.toString() }

        } else if (title.isNotBlank()) {
//             search with title only
            val doujinDetailItems = doujinDetailsDao.findByTitle(title)
            folderList = doujinDetailItems.map { item -> item.absolutePath.toString() }

        } else if (tags.isNotBlank()) {
            // search with tags only
            val tagList = tags.split(",")
                .map { tagName -> tagName }

            val doujinDetailItems = doujinDetailsDao.findByTags(tagList, tagList.size)
            folderList = doujinDetailItems.map { item -> item.absolutePath.toString() }
        }
        return folderList
    }

    private suspend fun findFoldersFromFileExplorer(query: String): List<String> {
//        val includedFolders = folderDao.getAllBlocking()
        val includedFolders = emptyList<IncludedPath>()

        val folderList = mutableListOf<File>()
        for (folder in includedFolders) {
            walk(folder.dir, folderList)
        }

        return folderList
            .filter { x -> x.toString().contains(query, true) }
            .map { x -> x.toString() }
    }

    private fun convertFolderPathToDoujin(path: String): Doujin? {
        val dir = File(path)

        val imageList = dir.listFiles(ImageFileFilter()).sorted()

        if (imageList.isNotEmpty()) {
            val coverImage = imageList.first().toUri()
            val title = dir.name
            val numberOfImages = imageList.size
            val lastModified = dir.lastModified()

            val doujin = Doujin(coverImage, title, numberOfImages, lastModified, dir)
            return doujin
        }

        return null
    }

    // Recursive method to search for directories & sub-directories
// todo: this method doesnt include the main directory itself, fix it
    private fun walk(currentDir: File, folderList: MutableList<File>) {
        for (f in currentDir.listFiles()) {
            if (f.isDirectory) {
                folderList.add(f)
                walk(f, folderList)
            }
        }
    }

}