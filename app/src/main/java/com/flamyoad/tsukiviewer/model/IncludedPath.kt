package com.flamyoad.tsukiviewer.model

import androidx.room.Entity
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.flamyoad.tsukiviewer.db.typeconverter.FolderConverter
import java.io.File

@Entity(tableName = "included_path")
@TypeConverters(FolderConverter::class)

data class IncludedPath(
    @PrimaryKey(OnConflictStrategy = OnConflictStrategy.IGNORE)
    val dir: File
)