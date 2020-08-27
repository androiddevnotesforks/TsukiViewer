package com.flamyoad.tsukiviewer.ui.home.local

import android.app.Application
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.flamyoad.tsukiviewer.model.Doujin
import com.flamyoad.tsukiviewer.model.IncludedFolder
import com.flamyoad.tsukiviewer.model.IncludedPath
import com.flamyoad.tsukiviewer.repository.MetadataRepository
import com.flamyoad.tsukiviewer.utils.ImageFileFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LocalDoujinViewModel(application: Application) : AndroidViewModel(application) {

    private val imageExtensions = arrayOf("jpg", "png", "gif", "jpeg")

    val repo = MetadataRepository(application)

    private val isFetchingDoujins = MutableLiveData<Boolean>(false)

    private val toastText = MutableLiveData<String?>(null)

    private val pathList: LiveData<List<IncludedPath>>

    private val folderList: LiveData<List<IncludedFolder>>

    private val doujinList = MutableLiveData<List<Doujin>>()

    private var jobScanFolder: Job = Job()

    fun isFetchingDoujins(): LiveData<Boolean> = isFetchingDoujins

    fun toastText(): LiveData<String?> = toastText

    fun folderList(): LiveData<List<IncludedFolder>> = folderList

    fun doujinList(): LiveData<List<Doujin>> = doujinList

    init {
        folderList = repo.folderDao.getAll()

        pathList = repo.pathDao.getAll()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("testbug", "ViewModel onCleared() called")
    }

    fun convertFoldersToDoujins(folders: List<IncludedFolder>) {
        viewModelScope.launch {
            isFetchingDoujins.value = true

            withContext(Dispatchers.Default) {
                for (f in folders) {
                    val dir = f.dir
                    val doujin = dir.toDoujin()

                    withContext(Dispatchers.Main) {
                        val currentList = doujinList.value?.toMutableList() ?: mutableListOf()
                        if (!currentList.contains(doujin)) {
                            currentList.add(doujin)
                            doujinList.value = currentList
                        }

                    }
                }
            }
            isFetchingDoujins.value = false
        }
    }

    fun filterRemovedFolders(includedFolder: List<IncludedFolder>) {
        viewModelScope.launch {
            isFetchingDoujins.value = true

            val oldDoujins = doujinList.value?.toMutableList() ?: mutableListOf()
            val newList = includedFolder.map { folder -> folder.dir }

            withContext(Dispatchers.Default) {

                for (i in oldDoujins.indices) {
                    val doujin = oldDoujins[i]
                    if (doujin.path !in newList) {
                        oldDoujins.removeAt(i)

                        withContext(Dispatchers.Main) {
                            doujinList.value = oldDoujins
                        }
                    }
                }
            }

            isFetchingDoujins.value = false
        }

    }

//    fun filterRemovedFolders(includedFolder: List<IncludedFolder>) {
//        viewModelScope.launch {
//            var includedDoujins: List<Doujin>
//
//            withContext(Dispatchers.Default) {
//                val list = doujinList.value?.toMutableList() ?: mutableListOf()
//                if (list.isNotEmpty()) {
//                    val includedDir = includedFolder.map { folder -> folder.dir }
//
//                    for (i in list.indices) {
//                        val doujin = list[i]
//                        if (doujin.path !in includedDir) {
//                            list.removeAt(i)
//
//                            withContext(Dispatchers.Main) {
//                                doujinList.value = list
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    fun checkForNewFolders() {
        jobScanFolder = viewModelScope.launch {
            isFetchingDoujins.value = true

            withContext(Dispatchers.IO) {
                val paths = repo.pathDao.getAllBlocking()
                for (path in paths) {
                    val subdirs = scanFoldersWithImages(path.dir, path.dir)
                }
            }

            isFetchingDoujins.value = false
        }
    }

    private suspend fun scanFoldersWithImages(parent: File, current: File) {
        if (current.isDirectory) {
            val childs = current.listFiles()
            if (childs.any { f -> f.extension in imageExtensions }) {
                repo.folderDao.insert(IncludedFolder(current, parent, current.name))
            }

            for (child in childs) {
                scanFoldersWithImages(parent, child)
            }
        }
    }

    fun cancelScan() {
        jobScanFolder.cancel()
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


//private suspend fun scanFolder(parent: File, current: File) {
//    if (current.isDirectory) {
//        Log.d("checkdir", current.toString())
//        if (current.hasImageFiles()) {
//            val includedFolder = IncludedFolder(current, parent, current.name)
//            repo.folderDao.insert(includedFolder)
//        }
//
//        val childDirectories = current.listFiles()
//        for (childDir in childDirectories) {
//            scanFolder(parent, childDir)
//        }
//    }
//}