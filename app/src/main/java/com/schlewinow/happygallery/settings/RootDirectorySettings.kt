package com.schlewinow.happygallery.settings

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.schlewinow.happygallery.model.GalleryNavigationData


object RootDirectorySettings {
    private const val sharedPreferencesAccess = "RootFolderSettings"

    private val rootDirectories : MutableList<DocumentFile> = mutableListOf()

    fun addRootDirectory(newDir: DocumentFile, context: Context) {
        rootDirectories.add(newDir)
        GalleryNavigationData.loadRootDirectoryGalleryContainers(newDir, context)
    }

    fun removeRootDirectory(removeDir: DocumentFile) {
        rootDirectories.remove(removeDir)
    }

    fun getRootDirectories(): List<DocumentFile> {
        return rootDirectories.toList()
    }

    fun restoreSettings(context: Context?) {
        if(context == null) {
            return
        }

        val prefs = context.getSharedPreferences(sharedPreferencesAccess, Context.MODE_PRIVATE)
        val numberRootFolders = prefs.getInt(sharedPreferencesAccess + "NumberRootFolders", 0)

        rootDirectories.clear()
        for(index in 0..numberRootFolders) {
            val folderUri = prefs.getString(sharedPreferencesAccess + "RootFolder$index", null)
            if(folderUri != null) {
                val directory = DocumentFile.fromTreeUri(context, Uri.parse(folderUri))
                if(directory?.isDirectory == true) {
                    addRootDirectory(directory, context)
                }
            }
        }
    }

    fun storeSettings(context: Context?) {
        if (context == null) {
            return
        }

        val prefs = context.getSharedPreferences(sharedPreferencesAccess, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()

        editor.putInt(sharedPreferencesAccess + "NumberRootFolders", rootDirectories.size)
        for((index, folder) in rootDirectories.withIndex()) {
            editor.putString(sharedPreferencesAccess + "RootFolder$index", folder.uri.toString())
        }

        editor.apply()
    }
}