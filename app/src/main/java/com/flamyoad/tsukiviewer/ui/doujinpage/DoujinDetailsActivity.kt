package com.flamyoad.tsukiviewer.ui.doujinpage

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.flamyoad.tsukiviewer.R
import com.flamyoad.tsukiviewer.adapter.DoujinPagerAdapter
import com.flamyoad.tsukiviewer.adapter.LocalDoujinsAdapter
import com.flamyoad.tsukiviewer.network.FetchMetadataService
import com.flamyoad.tsukiviewer.ui.editor.EditorActivity
import com.flamyoad.tsukiviewer.utils.snackbar
import com.flamyoad.tsukiviewer.utils.toast
import kotlinx.android.synthetic.main.activity_doujin_details.*

class DoujinDetailsActivity : AppCompatActivity() {

    private lateinit var viewModel: DoujinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doujin_details)

        viewModel = ViewModelProvider(this).get(DoujinViewModel::class.java)
        handleIntent()
        initTabLayout()
        initToolbar()

        viewModel.snackbarText.observe(this, Observer { text ->
            if (text.isNullOrBlank()) {
                return@Observer
            }

            snackbar(rootView, text, lengthLong = true)

            viewModel.snackbarText.value = ""
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_doujin_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
            }

            R.id.action_sync -> {
                syncMetadata()
            }

            R.id.action_open_in_browser -> {
                openBrowser()
            }

            R.id.action_clear_metadata -> {
                openClearDataDialog()
            }

            R.id.action_edit -> {
                val dirPath = intent.getStringExtra(LocalDoujinsAdapter.DOUJIN_FILE_PATH)
                val doujinTitle = intent.getStringExtra(LocalDoujinsAdapter.DOUJIN_NAME)

                val newIntent = Intent(this, EditorActivity::class.java)
                newIntent.putExtra(LocalDoujinsAdapter.DOUJIN_FILE_PATH, dirPath)
                newIntent.putExtra(LocalDoujinsAdapter.DOUJIN_NAME, doujinTitle)

                startActivity(newIntent)
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleIntent() {
        val dirPath = intent.getStringExtra(LocalDoujinsAdapter.DOUJIN_FILE_PATH)
        viewModel.scanForImages(dirPath)
    }

    private fun initTabLayout() {
        val adapterViewPager = DoujinPagerAdapter(supportFragmentManager)
        viewpager.adapter = adapterViewPager
        tabLayout.setupWithViewPager(viewpager)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel.detailWithTags.observe(this, Observer {
            if (it != null) {
                supportActionBar?.title = it.doujinDetails.shortTitleEnglish
            } else {
                supportActionBar?.title = "Doujin Details"
            }
        })
    }

    private fun syncMetadata() {
        if (viewModel.detailsNotExists()) {
            val dirPath = intent.getStringExtra(LocalDoujinsAdapter.DOUJIN_FILE_PATH)
            FetchMetadataService.startService(this, dirPath)
        } else {
            showConfirmResyncDialog()
        }
    }

    private fun showConfirmResyncDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Reset to original tags")
            .setMessage("Previous tags that have been added manually will be erased. Continue?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialogInterface, i ->
                // Update tags in DB
                viewModel.resetTags()
                dialogInterface.dismiss()
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
            })
            .create()

        dialog.show()
    }

    private fun openClearDataDialog() {
        val dialog = DialogRemoveMetadata.newInstance()
        dialog.show(supportFragmentManager, "clearDataDialog")
    }

    private fun openBrowser() {
        val nukeCode = viewModel.getNukeCode()

        val name = intent.getStringExtra(LocalDoujinsAdapter.DOUJIN_NAME)

        val uri: Uri

        if (nukeCode != null) {
            val address = "https://nhentai.net/g/${nukeCode}"
            uri = Uri.parse(address)
        } else {
            // "https://nhentai.net/search/?q=${urlEncodedName}"
            uri = Uri.parse("https://nhentai.net/search")
                .buildUpon()
                .appendQueryParameter("q", name)
                .build()
        }

        val browserIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = uri
        }

        if (browserIntent.resolveActivity(packageManager) != null) {
            startActivity(browserIntent)
        } else {
            toast("Cannot open browser")
        }
    }
}
