package com.example.moviebooking.data.service

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CloudinaryService(private val context: Context) {
    private val TAG = "CloudinaryService"

    init {
        try {
            val config = mapOf(
                "cloud_name" to "dkno35cfs",
                "api_key" to "114415861344639",
                "api_secret" to "rXPWdJEoXVZIjlJP0ud67SJU_2U",
                "secure" to true
            )
            MediaManager.init(context, config)
            Log.d(TAG, "Cloudinary MediaManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Cloudinary: ${e.message}", e)
        }
    }

    suspend fun uploadImage(uri: Uri, folder: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting image upload to folder: $folder")
            
            suspendCancellableCoroutine { continuation ->
                val requestId = MediaManager.get()
                    .upload(uri)
                    .option("folder", folder)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Log.d(TAG, "Upload started for requestId: $requestId")
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = (bytes * 100 / totalBytes).toInt()
                            Log.d(TAG, "Upload progress: $progress%")
                        }

                        override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                            Log.d(TAG, "Upload successful for requestId: $requestId")
                            val url = resultData["secure_url"] as? String
                            if (url != null) {
                                Log.d(TAG, "Image URL: $url")
                                continuation.resume(url)
                            } else {
                                continuation.resumeWithException(Exception("URL not found in response"))
                            }
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            Log.e(TAG, "Upload error for requestId: $requestId, error: ${error.description}")
                            continuation.resumeWithException(Exception(error.description))
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            Log.d(TAG, "Upload rescheduled for requestId: $requestId")
                        }
                    })
                    .dispatch()

                continuation.invokeOnCancellation {
                    MediaManager.get().cancelRequest(requestId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image: ${e.message}", e)
            throw e
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CloudinaryService? = null

        fun getInstance(context: Context): CloudinaryService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CloudinaryService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
} 