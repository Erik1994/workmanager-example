/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.friendschat.workmanagerexmaple.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.friendschat.workmanagerexmaple.R
import com.friendschat.workmanagerexmaple.contant.IMAGE_MANIPULATION_WORK_NAME
import com.friendschat.workmanagerexmaple.contant.KEY_BLURE_LEVEL
import com.friendschat.workmanagerexmaple.contant.KEY_IMAGE_URI
import com.friendschat.workmanagerexmaple.contant.TAG_OUTPUT
import com.friendschat.workmanagerexmaple.workers.BlurWorker
import com.friendschat.workmanagerexmaple.workers.CleanupWorker
import com.friendschat.workmanagerexmaple.workers.SaveImageToFileWorker


class BlurViewModel(application: Application) : ViewModel() {

    private val workMnager = WorkManager.getInstance(application)
    val outputWorkInfos: LiveData<List<WorkInfo>>

    private var imageUri: Uri? = null
    var outputUri: Uri? = null

    init {
        imageUri = getImageUri(application.applicationContext)
        outputWorkInfos = workMnager.getWorkInfosByTagLiveData(TAG_OUTPUT)
    }
    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    internal fun applyBlur(blurLevel: Int) {
        //workMnager.enqueue(OneTimeWorkRequest.from(BlurWorker::class.java))

//        val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
//            .setInputData(createInputDataForUri())
//            .build()
//        workMnager.enqueue(blurRequest)

        //Add workRequest to Cleanup temprary images
//        var continuation = workMnager
//            .beginWith(OneTimeWorkRequest
//                .from(CleanupWorker::class.java))

        var continuation = workMnager
            .beginUniqueWork(
                IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanupWorker::class.java))

        //Add WorkRequest to blur the image
        val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
            .setInputData(createInputData(blurLevel))
            .build()

        continuation = continuation.then(blurRequest)

//        for (i in 0 until blurLevel) {
//            val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
//
//            // Input the Uri if this is the first blur operation
//            // After the first blur operation the input will be the output of previous
//            // blur operations.
//            if (i == 0) {
//                blurBuilder.setInputData(createInputDataForUri())
//            }
//
//            continuation = continuation.then(blurBuilder.build())
//        }

        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Add WorkRequest to save the image to the filesystem
        val saveRequest = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .setConstraints(constraint)
            .addTag(TAG_OUTPUT)
            .build()

        continuation = continuation.then(saveRequest)

        continuation.enqueue()
    }


    fun cancelWork() {
        workMnager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    private fun createInputDataForUri(): Data.Builder = Data.Builder().apply {
        imageUri?.let {
            putString(KEY_IMAGE_URI, it.toString())
        }
    }

    private fun createInputDataForBluringLevel(level: Int): Data.Builder = createInputDataForUri().putInt(
        KEY_BLURE_LEVEL, level)

    private fun createInputData(level: Int): Data = createInputDataForBluringLevel(level).build()

    private fun getImageUri(context: Context): Uri {
        val resources = context.resources

        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.android_cupcake))
            .appendPath(resources.getResourceTypeName(R.drawable.android_cupcake))
            .appendPath(resources.getResourceEntryName(R.drawable.android_cupcake))
            .build()
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }
}
