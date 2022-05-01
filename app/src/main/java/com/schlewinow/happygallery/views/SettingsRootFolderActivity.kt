package com.schlewinow.happygallery.views

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.schlewinow.happygallery.R
import com.schlewinow.happygallery.model.GalleryNavigationData
import com.schlewinow.happygallery.settings.RootDirectorySettings

class SettingsRootFolderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_root_folder)
        setSupportActionBar(findViewById(R.id.settingsRootFolderToolbar))

        supportActionBar?.title = resources.getString(R.string.settings_root_folder_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRootFolders()

        val addButton: Button = findViewById(R.id.settingsRootFolderAddButton)
        addButton.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            folderSelectionResultLauncher.launch(intent)
        }
    }

    val folderSelectionResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val returnedIntent: Intent? = result.data
            if(returnedIntent != null && returnedIntent.data != null) {
                val directoryUri = returnedIntent.data!!
                contentResolver.takePersistableUriPermission(directoryUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                val newRootFolder = DocumentFile.fromTreeUri(this, returnedIntent.data!!)
                if(newRootFolder?.isDirectory ?: false) {
                    RootDirectorySettings.addRootDirectory(newRootFolder!!, this)
                    RootDirectorySettings.storeSettings(this);
                    setupRootFolders()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
            item.itemId == android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupRootFolders() {
        val rootFolderRecycler = findViewById<RecyclerView>(R.id.settingsRootFolderRecycler)

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rootFolderRecycler.layoutManager = linearLayoutManager

        rootFolderRecycler.adapter = RootFolderRecyclerAdapter()
    }

    inner class RootFolderRecyclerAdapter : RecyclerView.Adapter<RootFolderEntryHolder>() {
        private var folders = RootDirectorySettings.getRootDirectories()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RootFolderEntryHolder {
            val view =  LayoutInflater.from(this@SettingsRootFolderActivity).inflate(R.layout.element_root_folder_recycler, parent, false)
            return RootFolderEntryHolder(view)
        }

        override fun onBindViewHolder(holder: RootFolderEntryHolder, position: Int) {
            holder.setup(folders[position])
        }

        override fun getItemCount(): Int {
            return folders.size
        }
    }

    inner class RootFolderEntryHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun setup(file: DocumentFile) {
            val nameText: TextView = view.findViewById(R.id.rootFolderElementName)
            nameText.text = file.name

            val pathText: TextView = view.findViewById(R.id.rootFolderElementPath)
            pathText.text = file.uri.path

            val removeButton: Button = view.findViewById(R.id.rootFolderElementRemoveButton)
            removeButton.setOnClickListener { v ->
                RootDirectorySettings.removeRootDirectory(file)
                RootDirectorySettings.storeSettings(this@SettingsRootFolderActivity)
                setupRootFolders()
                GalleryNavigationData.removeRootDirectoryGalleryContainers(file.uri)
            }
        }
    }
}