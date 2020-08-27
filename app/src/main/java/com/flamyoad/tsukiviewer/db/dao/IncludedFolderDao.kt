package com.flamyoad.tsukiviewer.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flamyoad.tsukiviewer.model.IncludedFolder
import com.flamyoad.tsukiviewer.model.IncludedPathWithFolders

@Dao
interface IncludedFolderDao {

    @Query("SELECT * FROM included_folders")
    fun getAll(): LiveData<List<IncludedFolder>>

    @Query("SELECT * FROM included_path")
    suspend fun getPathWithFolders(): List<IncludedPathWithFolders>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(folder: IncludedFolder)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(list: List<IncludedFolder>)
}