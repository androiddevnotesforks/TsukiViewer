package com.flamyoad.tsukiviewer.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.flamyoad.tsukiviewer.model.IncludedFolder
import com.flamyoad.tsukiviewer.model.IncludedPath
import java.io.File

@Dao
interface IncludedPathDao {

    @Query("SELECT * FROM included_path")
    fun getAll(): LiveData<List<IncludedPath>>

    @Query("SELECT * FROM included_path")
    suspend fun getAllBlocking(): List<IncludedPath>

    @Insert
    suspend fun insert(path: IncludedPath)

    @Delete
    suspend fun delete(path: IncludedPath)

    @Query("DELETE FROM included_path WHERE dir = :pathName")
    suspend fun delete(pathName: String)

    @Query("DELETE FROM included_folders WHERE parentDir = :pathName")
    suspend fun removeIncludedFolder(pathName: String)

    @Transaction
    suspend fun removePathAndDirs(pathName: String) {
        delete(pathName)
        removeIncludedFolder(pathName)
    }
}