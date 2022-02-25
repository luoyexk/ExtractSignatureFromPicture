package com.zwl.extracttextfrompicture

import android.app.Application
import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType.CV_8UC4
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY
import java.lang.ref.WeakReference

class OpenCVManager {

    private var mLoaderCallback: OpenCVLoaderCallback? = null
    private var app: WeakReference<Application>? = null

    /**
     * initialize OpenCV sdk.
     */
    fun onResume(application: Application) {
        app = WeakReference(application)
        initCallback(application)
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, application, mLoaderCallback)
        } else {
            mLoaderCallback?.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    @WorkerThread
    fun getText(bitmap: Bitmap): Bitmap? {

        val src = Mat().apply {
            Utils.bitmapToMat(bitmap, this)
        }

        val gray = Mat().apply {
            Imgproc.cvtColor(src, this, COLOR_BGR2GRAY)
        }

        val thresh = Mat().apply {
            // thresh 羽度
            val thresh = 100.0
            val maxval = 255.0
            Imgproc.threshold(gray, this, thresh, maxval, Imgproc.THRESH_BINARY)
        }

        val argb = Mat().apply {
            Imgproc.cvtColor(thresh, this, Imgproc.COLOR_GRAY2BGRA)
        }

        setAlphaForChannel(argb)

        val result = Bitmap.createBitmap(argb.width(), argb.height(), Bitmap.Config.ARGB_8888).apply {
            Utils.matToBitmap(argb, this)
        }

        return result
    }

    private fun setAlphaForChannel(mat: Mat) {
        val rows = mat.rows()
        val cols = mat.cols()
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val bgra = mat[i, j]
                bgra[3] = 0.0 // Alpha
                val doubles = mat[i, j]
            }
        }
    }

    private fun initCallback(application: Application) {
        if (mLoaderCallback == null) {
            mLoaderCallback = OpenCVLoaderCallback(application)
        }
    }

}