package com.flamyoad.tsukiviewer.utils.extensions

import androidx.core.net.toUri
import com.flamyoad.tsukiviewer.model.Doujin
import com.flamyoad.tsukiviewer.utils.ImageFileFilter
import java.io.File

fun File.toDoujin(): Doujin? {
    val imageList = this.listFiles(ImageFileFilter()) ?: return null

    val doujin = Doujin(
        pic = imageList.first().toUri(),
        title = this.name,
        path = this,
        lastModified = this.lastModified(),
        numberOfItems = imageList.size
    )
    return doujin
}