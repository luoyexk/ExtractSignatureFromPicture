package com.zwl.extracttextfrompicture

import android.app.Application
import android.util.Log
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface

class OpenCVLoaderCallback(application: Application) : BaseLoaderCallback(application) {
    var isInitSuc = false
    override fun onManagerConnected(status: Int) {
        when (status) {
            LoaderCallbackInterface.SUCCESS -> {
                isInitSuc = true
                Log.d(TAG, "OpenCV loaded successfully")
            }
            else -> {
                isInitSuc = false
                super.onManagerConnected(status)
            }
        }
    }
}