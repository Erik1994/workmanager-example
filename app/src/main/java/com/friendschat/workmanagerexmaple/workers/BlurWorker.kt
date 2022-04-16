package com.friendschat.workmanagerexmaple.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.friendschat.workmanagerexmaple.contant.KEY_BLURE_LEVEL
import com.friendschat.workmanagerexmaple.contant.KEY_IMAGE_URI
import com.friendschat.workmanagerexmaple.contant.PROGRESS
import com.friendschat.workmanagerexmaple.util.blurBitmap
import com.friendschat.workmanagerexmaple.util.makeStatusNotification
import com.friendschat.workmanagerexmaple.util.sleep
import com.friendschat.workmanagerexmaple.util.writeBitmapToFile

private const val TAG = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        val appContext = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val blureLevel = inputData.getInt(KEY_BLURE_LEVEL, 1)
        makeStatusNotification("Bluring iamge", appContext)

       // sleep()

        (0..100 step 10).forEach {
            setProgressAsync(workDataOf(PROGRESS to it))
            sleep()
        }


        return try {
//            val picture = BitmapFactory.decodeResource(
//                appContext.resources,
//                R.drawable.android_cupcake)

            if (TextUtils.isEmpty(resourceUri)) {
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = appContext.contentResolver

            var picture = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )

            repeat(blureLevel) {
                picture = blurBitmap(picture, appContext)
            }

            //write bitmap to a temp file
            val outputUri = writeBitmapToFile(appContext, picture)

            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())

            Result.success(outputData)
        } catch (throwable: Throwable) {
            Log.e(TAG, "Error applying blur")
            Result.failure()
        }
    }
}