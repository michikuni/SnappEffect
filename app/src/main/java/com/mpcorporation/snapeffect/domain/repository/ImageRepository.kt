package com.mpcorporation.snapeffect.domain.repository

import android.graphics.Bitmap
import android.net.Uri

interface ImageRepository {
    suspend fun decode(uri: Uri): Bitmap
    suspend fun saveToCache(bitmap: Bitmap, filename: String): Uri
    suspend fun saveToGallery(folderName: String, filename: String, bitmap: Bitmap): Uri
    suspend fun clearTemporaryFiles()
}
