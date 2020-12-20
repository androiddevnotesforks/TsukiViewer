package com.flamyoad.tsukiviewer.ui.home.tags

import android.app.Application
import androidx.lifecycle.*
import com.flamyoad.tsukiviewer.db.AppDatabase
import com.flamyoad.tsukiviewer.db.dao.DoujinTagsDao
import com.flamyoad.tsukiviewer.db.dao.TagDao
import com.flamyoad.tsukiviewer.model.Tag
import com.flamyoad.tsukiviewer.model.TagSortingMode
import com.flamyoad.tsukiviewer.model.TagType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DoujinTagsViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase = AppDatabase.getInstance(application)

    private val tagDao: TagDao
    private val doujinTagDao: DoujinTagsDao

    // First object in the Pair : Keyword of the search
    // Second object in the Pair: The sorting mode (e.g. Sort by number of items ascending)
    private val searchTerms = MutableLiveData<Pair<String, TagSortingMode>>()

    fun searchTerms(): LiveData<Pair<String, TagSortingMode>> = searchTerms

    private val allTags: LiveData<List<Tag>>
    private val parodies: LiveData<List<Tag>>
    private val characters: LiveData<List<Tag>>
    private val tags: LiveData<List<Tag>>
    private val artists: LiveData<List<Tag>>
    private val groups: LiveData<List<Tag>>
    private val languages: LiveData<List<Tag>>
    private val categories: LiveData<List<Tag>>

    init {
        tagDao = db.tagsDao()
        doujinTagDao = db.doujinTagDao()

        searchTerms.value = Pair("", TagSortingMode.NAME_ASCENDING)

        allTags = searchTerms.switchMap { searchTerm ->
            findFilteredItems(TagType.All, searchTerm)
        }

        parodies = searchTerms.switchMap { searchTerm ->
            findFilteredItems(TagType.Parodies, searchTerm)
        }

        characters = searchTerms.switchMap { searchTerm ->
            findFilteredItems(TagType.Characters, searchTerm)
        }

        tags = searchTerms.switchMap { searchTerm ->
            findFilteredItems(TagType.Tags, searchTerm)
        }

        artists = searchTerms.switchMap { searchTerm ->
            findFilteredItems(TagType.Artists, searchTerm)
        }

        groups = searchTerms.switchMap { searchTerm ->
            findFilteredItems(TagType.Groups, searchTerm)
        }

        languages = searchTerms.switchMap { searchTerm ->
            findFilteredItems(TagType.Languages, searchTerm)
        }

        categories = searchTerms.switchMap { searchTerm ->
            findFilteredItems(TagType.Categories, searchTerm)
        }
    }

    private fun findFilteredItems(
        tagType: TagType,
        searchTerm: Pair<String, TagSortingMode>
    ): LiveData<List<Tag>> {
        val keyword = searchTerm.first
        val sortingMode = searchTerm.second

        return when (tagType) {
            TagType.All -> tagDao.getAllWithFilter(keyword, sortingMode)
            else -> tagDao.getByCategoryWithFilter(tagType.getLowerCaseName(), keyword, sortingMode)
        }
    }

    fun getTagItems(type: TagType): LiveData<List<Tag>> {
        return when (type) {
            TagType.All -> allTags
            TagType.Parodies -> parodies
            TagType.Characters -> characters
            TagType.Tags -> tags
            TagType.Artists -> artists
            TagType.Groups -> groups
            TagType.Languages -> languages
            TagType.Categories -> categories
        }
    }

    fun getCurrentMode(): TagSortingMode {
        return searchTerms.value?.second
            ?: TagSortingMode.NAME_ASCENDING
    }

    fun setQuery(keyword: String) {
        val prevSortingMode = searchTerms.value?.second ?: TagSortingMode.NAME_ASCENDING
        val searchTerm = Pair(keyword, prevSortingMode)

        searchTerms.value = searchTerm
    }

    fun setSortingMode(mode: TagSortingMode) {
        val prevKeyword = searchTerms.value?.first ?: ""

        val searchTerm = Pair(prevKeyword, mode)

        searchTerms.value = searchTerm
    }

    fun removeTag(tagId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            doujinTagDao.deleteFromTag(tagId)
            tagDao.delete(tagId)
        }
    }
}