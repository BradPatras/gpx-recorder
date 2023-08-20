package com.iboism.gpxrecorder.export

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.DialogFragment
import com.iboism.gpxrecorder.Keys
import com.iboism.gpxrecorder.R
import com.iboism.gpxrecorder.databinding.FragmentExportBinding
import com.iboism.gpxrecorder.util.FileHelper
import com.iboism.gpxrecorder.util.Holder
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ExportFragment: DialogFragment() {
    private lateinit var gpxId: Holder<Long>
    private val fileHelper: FileHelper? by lazy { FileHelper() }
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val compositeDisposable = CompositeDisposable()
    private val saveFileLauncher = registerForActivityResult(SaveFileContract()) { uri ->
        onSaveLocationSelected(uri)
    }

    private lateinit var binding: FragmentExportBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = FragmentExportBinding.inflate(inflater, container, container != null)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpxId = Holder(requireArguments().get(Keys.GpxId) as Long)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.exportShareBtn.setOnClickListener { onShareClicked() }
        binding.exportSaveBtn.setOnClickListener { onSaveClicked() }
        binding.formatSelectorSpinner.adapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.export_formats_array)
        )

        binding.exportProgressBar.isIndeterminate = true
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.exportSaveBtn.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        binding.exportShareBtn.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        binding.exportProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun onShareClicked() {
        val context = context ?: return
        setLoadingState(true)
        val export = fileHelper?.shareRouteFile(
            context,
            gpxId.value,
            getSelectedExportFormat()
        )?.subscribe {
            setLoadingState(false)
            dismiss()
        }
        export?.let { compositeDisposable.add(it) }
    }

    private fun onSaveClicked() {
        showSystemFolderPicker()
    }

    private fun showSystemFolderPicker() {
        val filename = fileHelper?.getRouteFilename(
            gpxId.value,
            getSelectedExportFormat()
        ) ?: return

        saveFileLauncher.launch(filename)
    }

    private fun onSaveLocationSelected(location: Uri?) {
        val destination = location ?: return
        val context = context ?: return
        setLoadingState(true)
        fileHelper?.apply {
            compositeDisposable.add(
                saveRouteFile(
                    context,
                    gpxId.value,
                    destination,
                    getSelectedExportFormat()
                )
                    .delay(1, TimeUnit.SECONDS)
                    .subscribe {
                        uiScope.launch {
                        setLoadingState(false)
                        dismiss()
                    }
                }
            )
        }
    }

    private fun getSelectedExportFormat(): FileHelper.Format {
        return when(binding.formatSelectorSpinner.selectedItemPosition) {
            0 -> FileHelper.Format.Gpx
            else -> FileHelper.Format.GeoJson
        }
    }

    override fun onDetach() {
        super.onDetach()
        compositeDisposable.clear()
    }

    class SaveFileContract : ActivityResultContract<String, Uri?>() {
        override fun createIntent(context: Context, input: String): Intent {
            return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/xml"

                putExtra(Intent.EXTRA_TITLE, input)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return if (resultCode == Activity.RESULT_OK) intent?.data else null
        }
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