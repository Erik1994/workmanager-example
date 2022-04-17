package com.friendschat.workmanagerexmaple

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkInfo
import com.friendschat.workmanagerexmaple.contant.KEY_IMAGE_URI
import com.friendschat.workmanagerexmaple.contant.PROGRESS
import com.friendschat.workmanagerexmaple.databinding.ActivityBlurBinding
import com.friendschat.workmanagerexmaple.viewmodel.BlurViewModel
import com.friendschat.workmanagerexmaple.viewmodel.BlurViewModelFactory

class BlurActivity : AppCompatActivity() {
    private val viewModel: BlurViewModel by viewModels {
        BlurViewModelFactory(
            application
        )
    }
    private lateinit var binding: ActivityBlurBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlurBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setClickListener()
        observeWorkInfoData()
    }

    private fun observeWorkInfoData() {
        viewModel.outputWorkInfos.observe(this) { workInfoList ->
            if (workInfoList.isNullOrEmpty()) return@observe

            val workInfo = workInfoList[0]

            if (workInfo.state.isFinished) {
                showWorkFinished()

                val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)

                if (!outputImageUri.isNullOrEmpty()) {
                    viewModel.setOutputUri(outputImageUri)
                    binding.seeFileButton.visibility = View.VISIBLE
                }
            } else {
                showWorkInProgress()
            }
        }

        viewModel.progressWorkInos.observe(this){ workInfoList ->
            if(workInfoList.isNullOrEmpty()) return@observe
            workInfoList.forEach { workInfo ->
                if (WorkInfo.State.RUNNING == workInfo.state) {
                    val progress = workInfo.progress.getInt(PROGRESS, 0)
                    binding.progressBar.progress = progress
                }
            }
        }
    }


    private fun setClickListener() {
        with(binding) {
            goButton.setOnClickListener { viewModel.applyBlur(blurLevel) }
            cancelButton.setOnClickListener { viewModel.cancelWork() }
            seeFileButton.setOnClickListener {
                viewModel.outputUri?.let {
                    val actionView = Intent(Intent.ACTION_VIEW, it)
                    actionView.resolveActivity(packageManager)?.run {
                        startActivity(actionView)
                    }
                }
            }
        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        with(binding) {
            progressBar.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            goButton.visibility = View.GONE
            seeFileButton.visibility = View.GONE
        }
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        with(binding) {
            progressBar.visibility = View.GONE
            cancelButton.visibility = View.GONE
            goButton.visibility = View.VISIBLE
            progressBar.progress = 0
        }
    }

    private val blurLevel: Int
        get() =
            when (binding.radioBlurGroup.checkedRadioButtonId) {
                R.id.radio_blur_lv_1 -> 1
                R.id.radio_blur_lv_2 -> 2
                R.id.radio_blur_lv_3 -> 3
                else -> 1
            }
}