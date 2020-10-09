package com.iboism.gpxrecorder.export

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.records.details.CREATE_FILE_INTENT_ID
import com.iboism.gpxrecorder.util.FileHelper
import com.iboism.gpxrecorder.util.Holder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_export.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class ExportFragment: DialogFragment() {
    private lateinit var gpxId: Holder<Long>
    private val fileHelper: FileHelper? by lazy { FileHelper() }
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.fragment_export, container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpxId = Holder(requireArguments().get(Keys.GpxId) as Long)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        export_share_btn?.setOnClickListener { onShareClicked() }
        export_save_btn?.setOnClickListener { onSaveClicked() }
        export_progress_bar.isIndeterminate = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            CREATE_FILE_INTENT_ID -> onSaveLocationSelected(data?.data)
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        export_save_btn.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        export_share_btn.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        export_progress_bar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun onShareClicked() {
        val context = context ?: return
        setLoadingState(true)
        val export = fileHelper?.shareGpxFile(context, gpxId.value)?.subscribe {
            setLoadingState(false)
            parentFragmentManager.popBackStack()
        }
        export?.let { compositeDisposable.add(it) }
    }

    private fun onSaveClicked() {
        showSystemFolderPicker()
    }

    private fun showSystemFolderPicker() {
        val context = context ?: return
        val filename = fileHelper?.getGpxFilename(context, gpxId.value) ?: return

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/xml"

            putExtra(Intent.EXTRA_TITLE, filename)
        }

        startActivityForResult(intent, CREATE_FILE_INTENT_ID)
    }

    private fun onSaveLocationSelected(location: Uri?) {
        val destination = location ?: return
        val context = context ?: return
        setLoadingState(true)
        fileHelper?.apply {
            saveGpxFile(context, gpxId.value, destination).delay(1, TimeUnit.SECONDS).subscribe {
                uiScope.launch {
                    setLoadingState(false)
                    this@ExportFragment.dismiss()
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        compositeDisposable.clear()
    }

    companion object {
        fun newInstance(gpxId: Long): ExportFragment {
            val args = Bundle()
            args.putLong(Keys.GpxId, gpxId)

            val fragment = ExportFragment()
            fragment.arguments = args

            return fragment
        }
    }
}