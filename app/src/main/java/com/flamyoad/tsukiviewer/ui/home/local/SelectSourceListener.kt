package com.flamyoad.tsukiviewer.ui.home.local

import com.flamyoad.tsukiviewer.model.Source
import java.util.*

interface SelectSourceListener {
    fun onFetchMetadata(sources: EnumSet<Source>)
}