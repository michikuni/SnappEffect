package com.mpcorporation.snapeffect.data.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.mpcorporation.snapeffect.domain.repository.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageRepository {

    override suspend fun decode(uri: Uri): Bitmap = BitmapIO.decode(context, uri)

    override suspend fun saveToCache(bitmap: Bitmap, filename: String): Uri =
        BitmapIO.saveToCache(context, bitmap, filename)

    override suspend fun saveToGallery(folderName: String, filename: String, bitmap: Bitmap): Uri =
        BitmapIO.saveToPictures(context, folderName, filename, bitmap)

    override suspend fun clearTemporaryFiles() {
        val tempFolder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Snap Effect Temporary"
        )
        if (tempFolder.exists() && tempFolder.isDirectory) {
            tempFolder.listFiles()?.forEach { f ->
                if (f.isFile && !f.delete()) {
                    Log.e(TAG, "Cannot delete: ${f.absolutePath}")
                }
            }
        }
        context.cacheDir.listFiles { f ->
            f.name.startsWith("snap_tmp_") || f.name.startsWith("cropped_")
        }?.forEach { it.delete() }
    }

    private companion object { const val TAG = "ImageRepository" }
}
