package com.zwl.extracttextfrompicture

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.ref.WeakReference

class HandleViewModel : ViewModel() {

    private val _srcUri = MutableStateFlow("".toUri())
    private val _dstUri = MutableStateFlow("".toUri())
    private val _processing = MutableStateFlow(false)
    private val _error = MutableStateFlow("")

    val srcUrl
        get() = _srcUri
    val dstUrl
        get() = _dstUri
    val onProcessing
        get() = _processing
    val error
        get() = _error

    private var cvEngine: WeakReference<OpenCVManager>? = null

    fun setUri(application: Application, uri: Uri) {
        viewModelScope.launch {
            _dstUri.emit("".toUri())
            _srcUri.emit(uri)
            startProcess(application, uri)
        }
    }

    private fun startProcess(application: Application, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _processing.emit(true)
            val cvManager = cvEngine?.get()
            if (cvManager != null) {

                val result = kotlin.runCatching {
                    val src = BitmapFactory.decodeStream(application.contentResolver.openInputStream(uri))
                    val bitmap = cvManager.getText(src)
                    bitmap
                }

                val bitmap = result.getOrNull()
                if (bitmap != null) {
                    val name = "${System.currentTimeMillis()}.png"
                    val file = File(application.filesDir, name)
                    val dstUri = file.toUri()
                    writeResultToStorage(file.absolutePath, bitmap)
                    _dstUri.emit(dstUri)
                }

                if (bitmap == null && result.isFailure) {
                    _error.emit(result.exceptionOrNull()?.message ?: "")
                }
            }

            _processing.emit(false)
        }
    }

    fun alterDocument(application: Application, empty: Uri, src: Uri) {
        viewModelScope.launch {
            saveDocument(application, empty, src)
                .onStart {
                    _processing.emit(true)
                }
                .onCompletion {
                    _processing.emit(false)
                }
                .catch {

                }
                .collect()
        }
    }

    private fun saveDocument(application: Application, empty: Uri, src: Uri): Flow<Unit> {
        return flow {
            val result = kotlin.runCatching {
                application.contentResolver.openFileDescriptor(empty, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { outputStream ->
                        outputStream.write(application.contentResolver.openInputStream(src)!!.readBytes())
                    }
                }
            }
            if (result.isFailure) {
                throw result.exceptionOrNull()!!
            }
            emit(Unit)
        }
    }


    private fun writeResultToStorage(path: String, bitmap: Bitmap) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, File(path).outputStream())
    }

    fun setCVEngine(openCVEngine: OpenCVManager) {
        cvEngine = WeakReference(openCVEngine)
    }

}