package com.flamyoad.tsukiviewer.model

import androidx.room.Embedded
import androidx.room.Relation

data class IncludedPathWithFolders(
    @Embedded val path: IncludedPath,

    @Relation(parentColumn = "dir", entity = IncludedFolder::class, entityColumn = "parentDir")
    val folders: List<IncludedFolder>
)