package com.flamyoad.tsukiviewer.model

import androidx.room.*
import com.flamyoad.tsukiviewer.db.typeconverter.FolderConverter
import java.io.File

@Entity(tableName = "included_folders")

@ForeignKey(entity = IncludedPath::class,
    parentColumns = ["dir"],
    childColumns = ["parentDir"],
    onDelete = ForeignKey.CASCADE)

@TypeConverters(FolderConverter::class)
data class IncludedFolder(
    @PrimaryKey
    val dir: File,

    val parentDir: File,

    val lastName: String
)