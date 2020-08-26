package com.flamyoad.tsukiviewer.ui.home.local

import android.app.Application
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.flamyoad.tsukiviewer.model.Doujin
import com.flamyoad.tsukiviewer.model.IncludedFolder
import com.flamyoad.tsukiviewer.repository.MetadataRepository
import com.flamyoad.tsukiviewer.utils.ImageFileFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class LocalDoujinViewModel(application: Application) : AndroidViewModel(application) {

    val repo = MetadataRepository(application)

    private val isFetchingDoujins = MutableLiveData<Boolean>(false)

    private val toastText = MutableLiveData<String?>(null)

    private val folderList: LiveData<List<IncludedFolder>>

    private val tempFolders = MutableLiveData<MutableList<Doujin>>()

    val doujinList = MediatorLiveData<List<Doujin>>()

    fun isFetchingDoujins(): LiveData<Boolean> = isFetchingDoujins

    fun toastText(): LiveData<String?> = toastText

    fun doujinList(): LiveData<List<Doujin>> = doujinList

    init {
        folderList = repo.folderDao.getAll()

        doujinList.apply {
            addSource(folderList) { convertFoldersToDoujins(it) }
            addSource(tempFolders) { value = it }
        }
    }

    private fun convertFoldersToDoujins(folders: List<IncludedFolder>) {
        viewModelScope.launch {
            isFetchingDoujins.value = true

            withContext(Dispatchers.IO) {
                val newList = folders.map { f -> f.dir }
                val oldList = tempFolders.value?.map { doujin -> doujin.path } ?: emptyList()

                val itemsToBeAdded = newList.minus(oldList)

                for (file in itemsToBeAdded) {
                    val doujin = file.toDoujin()

                    withContext(Dispatchers.Main) {
                        val currentDoujins = tempFolders.value ?: mutableListOf()
                        currentDoujins.add(doujin)

                        tempFolders.value = currentDoujins
                    }
                }
            }

            isFetchingDoujins.value = false
        }
    }

    fun listFolders() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val pathWithFolders = repo.folderDao.getPathWithFolders()

                pathWithFolders.forEach {
                    val parentDir = it.path.dir
                    val folders = it.folders

                    var amountOfDoujins = 0

                    folders.forEach { item ->
                        if (item.isDoujin()) {
                            amountOfDoujins++
                        }
                    }

                    if (amountOfDoujins != getSubdirectoriesCount(parentDir)) {
                        scanForNewFolders(parentDir)
                    }
                }
            }
        }
    }

    private fun getSubdirectoriesCount(dir: File): Int {
        val subDirectories = dir.walk()
            .map { f -> f.isDirectory }
        return subDirectories.count()
    }

    private suspend fun scanForNewFolders(dir: File) {
        val subDirectories = dir.walk()
            .filter { f -> f.hasImageFiles() }
            .map { f -> IncludedFolder(f, dir, f.name) }
            .toList()

        repo.folderDao.insertAll(subDirectories)
    }

    fun File.toDoujin(): Doujin {
        val imageList = this.listFiles(ImageFileFilter())

        val doujin = Doujin(
            pic = imageList.first().toUri(),
            title = this.name,
            path = this,
            lastModified = this.lastModified(),
            numberOfItems = imageList.size
        )
        return doujin
    }

    private fun IncludedFolder.isDoujin(): Boolean {
        val curentDir = this.dir

        if (!curentDir.isDirectory)
            return false

        val imageList = curentDir.listFiles(ImageFileFilter())

        val hasImages = !imageList.isNullOrEmpty()

        return hasImages
    }

    private fun File.hasImageFiles(): Boolean {
        val imageList = this.listFiles(ImageFileFilter())
        return !imageList.isNullOrEmpty()
    }

    suspend fun fetchMetadataAll(dirList: List<File>) {
        isFetchingDoujins.value = true

        val amountFetched = repo.fetchMetadataAll(dirList)

        if (amountFetched == 0) {
            toastText.value = "All items are already synced"

        } else {
            val noun = if (amountFetched > 1)
                "items"
            else
                "item"

            toastText.value = "Sync done for $amountFetched $noun"
        }

        isFetchingDoujins.value = false
    }
}

// This method is recursive
//    private suspend fun scanForSubdirectories(currentDir: File, parentDir: File, list: MutableList<IncludedFolder>) {
//        if (currentDir.hasImageFiles()) {
//            list.add(IncludedFolder(currentDir, parentDir, currentDir.name))
//        }
//
//        for (f in currentDir.listFiles()) {
//            if (f.isDirectory) {
//                if (f.hasImageFiles()) {
//                    list.add(IncludedFolder(f, parentDir, f.name))
//                }
//                scanForSubdirectories(f, parentDir, list)
//            }
//        }
//    }

//// Recursive method to search for directories & sub-directories
//// todo: this method doesnt include the main directory itself, fix it
//private suspend fun walk(currentDir: File, list: MutableList<IncludedFolder>) {
//    for (f in currentDir.listFiles()) {
//        if (f.isDirectory) {
//            val imageList = f.listFiles(ImageFileFilter()).sorted()
//
//            if (imageList.isNotEmpty()) {
//                val coverImage = imageList.first().toUri()
//                val title = f.name
//                val numberOfImages = imageList.size
//                val lastModified = f.lastModified()
//
//                doujinList.add(
//                    Doujin(coverImage, title, numberOfImages, lastModified, f)
//                )
//            }
//
//            walk(f)
//        }
//    }
//}