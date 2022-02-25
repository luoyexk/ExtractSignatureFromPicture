package com.zwl.extracttextfrompicture

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogRepo {
    fun showSelectPictureDialog(context: Context, onSelectCamera: () -> Unit, onSelectGallery: () -> Unit) {
        val items = arrayOf(
            context.getText(R.string.open_camera),
            context.getText(R.string.open_gallery),
        )
        MaterialAlertDialogBuilder(context)
            .setItems(items) { dialog, which ->
                when (which) {
                    0 -> {
                        onSelectCamera()
                    }
                    1 -> {
                        onSelectGallery()
                    }
                }
                dialog.dismiss()
            }
            .show()
    }

    fun showErrorMessage(context: Context, message: CharSequence) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.error)
            .setMessage(message)
            .show()
    }
}