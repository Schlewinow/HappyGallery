package com.schlewinow.happygallery.settings

import android.content.Context
import com.schlewinow.happygallery.tools.sorting.BaseFileComparator
import com.schlewinow.happygallery.tools.sorting.FileDateComparator
import com.schlewinow.happygallery.tools.sorting.FileNameComparator
import com.schlewinow.happygallery.tools.sorting.FileSizeComparator

object GallerySettings {
    private const val sharedPreferencesAccess = "GallerySettings"
    private const val defaultFileColumnsPortrait = 3
    private const val defaultFileColumnsLandscape = 4
    private val defaultFileComparator = FileDateComparator(true)

    var fileColumnsPortrait: Int = defaultFileColumnsPortrait
        set(value) {
            if(value < 1) field = 1
            else if(value > 10) field = 10
            else field = value
        }

    var fileColumnsLandscape: Int = defaultFileColumnsLandscape
        set(value) {
            if(value < 1) field = 1
            else if(value > 10) field = 10
            else field = value
        }

    var sortingComparator: BaseFileComparator = defaultFileComparator

    fun restoreSettings(context: Context?) {
        if(context == null) {
            return
        }

        val prefs = context.getSharedPreferences(sharedPreferencesAccess, Context.MODE_PRIVATE)
        fileColumnsPortrait = prefs.getInt(sharedPreferencesAccess + "FileColumnsPortrait", defaultFileColumnsPortrait)
        fileColumnsLandscape= prefs.getInt(sharedPreferencesAccess + "FileColumnsLandscape", defaultFileColumnsLandscape)
        sortingComparator = stringToComparator(prefs.getString(sharedPreferencesAccess + "SortingComparator", null))
    }

    fun storeSettings(context: Context?) {
        if(context == null) {
            return
        }

        val prefs = context.getSharedPreferences(sharedPreferencesAccess, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putInt(sharedPreferencesAccess + "FileColumnsPortrait", fileColumnsPortrait)
        editor.putInt(sharedPreferencesAccess + "FileColumnsLandscape", fileColumnsLandscape)

        val sortingComparatorString = comparatorToString(sortingComparator)
        if(sortingComparatorString != null) {
            editor.putString(sharedPreferencesAccess + "SortingComparator", sortingComparatorString)
        }

        editor.apply()
    }

    /**
     * Creates a string to identify the comparator type and required parameters for storage in shared preferences.
     */
    private fun comparatorToString(fileComparator: BaseFileComparator) : String? {
        var output: String?

        when {
            fileComparator is FileNameComparator -> output = "Name"
            fileComparator is FileSizeComparator -> output = "Size"
            fileComparator is FileDateComparator -> output = "LastModified"
            else -> output = null
        }

        if(output != null) {
            output += "_${fileComparator.ascending}"
        }

        return output
    }

    /**
     * Pares a string to identify the comparator type and required parameters from storage in shared preferences.
     */
    private fun stringToComparator(fileComparatorString: String?) : BaseFileComparator {
        if(fileComparatorString == null) {
            return defaultFileComparator
        }

        val name = fileComparatorString.split("_")[0]
        val ascendingString = fileComparatorString.split("_")[1]
        val ascending = ascendingString.toBooleanStrict()

        return when(name) {
            "Name" -> FileNameComparator(ascending)
            "Size" -> FileSizeComparator(ascending)
            "LastModified" -> FileDateComparator(ascending)
            else -> defaultFileComparator
        }
    }
}