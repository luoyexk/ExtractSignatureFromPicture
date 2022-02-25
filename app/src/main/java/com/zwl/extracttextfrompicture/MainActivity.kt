package com.zwl.extracttextfrompicture

import android.app.Application
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.zwl.extracttextfrompicture.databinding.ActivityMainBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

const val TAG = "zzzz"

class MainActivity : AppCompatActivity() {

    private val openCVEngine = OpenCVManager()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val mViewModel by viewModels<HandleViewModel>()
    private val mTakePictureResult = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            onGetSourceUri()
        } else {
            outputUri = null
        }
    }

    private val mPickPictureResult = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        outputUri = it
        onGetSourceUri()
    }

    private val mSavePictureResult = registerForActivityResult(ActivityResultContracts.CreateDocument()) { newDocument ->
        mViewModel.alterDocument(application, newDocument, mViewModel.dstUrl.value)
    }

    private var outputUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener {
            DialogRepo().showSelectPictureDialog(
                this,
                onSelectCamera = {
                    val picturePath = File(cacheDir, "PictureToText.tmp")
                    val input = FileProvider.getUriForFile(this, "$packageName.fileprovider", picturePath)
                    outputUri = input
                    mTakePictureResult.launch(input)
                },
                onSelectGallery = {
                    val contentType = arrayOf(
                        "image/*",
                    )
                    mPickPictureResult.launch(contentType)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        openCVEngine.onResume(application)
        mViewModel.setCVEngine(openCVEngine)
    }

    private fun onGetSourceUri() {
        outputUri?.let {
            mViewModel.setUri(application, it)
        }
        outputUri = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                saveFile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveFile() {
        createFile()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun createFile() {
        mSavePictureResult.launch("file.png")
    }

}