package com.iboism.gpxrecorder.export

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.graphics.drawable.toDrawable
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
    sealed interface Action {
        object Share: Action
        object Save: Action
        object Download: Action
    }

    private lateinit var gpxIds: Holder<List<Long>>
    private val fileHelper: FileHelper? by lazy { FileHelper() }
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: FragmentExportBinding
    private var actions: List<Action> = emptyList()
    private val saveFileLauncher = registerForActivityResult(SaveFileContract()) { uri ->
        onSaveLocationSelected(uri)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        binding = FragmentExportBinding.inflate(inflater, container, container != null)

        if (!actions.contains(Action.Download)) {
            binding.exportDownloadBtn.visibility = View.GONE
            binding.infoIcon.visibility = View.GONE
            binding.infoLabel.visibility = View.GONE
        }
        if (!actions.contains(Action.Save)) {
            binding.exportSaveBtn.visibility = View.GONE
        }
        if(!actions.contains(Action.Share)) {
            binding.exportShareBtn.visibility = View.GONE
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpxIds = Holder(requireArguments().getLongArray(Keys.GpxId)?.toList() ?: listOf())
        val bundleActions = mutableListOf<Action>()
        if (requireArguments().getBoolean("canSave")) {
            bundleActions.add(Action.Save)
        }
        if (requireArguments().getBoolean("canShare")) {
            bundleActions.add(Action.Share)
        }
        if (requireArguments().getBoolean("canDownload")) {
            bundleActions.add(Action.Download)
        }
        this.actions = bundleActions
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.exportShareBtn.setOnClickListener { onShareClicked() }
        binding.exportSaveBtn.setOnClickListener { onSaveClicked() }
        binding.exportDownloadBtn.setOnClickListener { onDownloadClicked() }
        binding.formatSelectorSpinner.adapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.export_formats_array)
        )

        binding.exportProgressBar.isIndeterminate = true
    }

    private fun setLoadingState(isLoading: Boolean) {
        setActionButtonsHidden(isLoading)
        binding.exportProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setActionButtonsHidden(hidden: Boolean) {
        if (actions.contains(Action.Save)) {
            binding.exportSaveBtn.visibility = if (hidden) View.GONE else View.VISIBLE
        }

        if (actions.contains(Action.Share)) {
            binding.exportShareBtn.visibility = if (hidden) View.GONE else View.VISIBLE
        }

        if (actions.contains(Action.Download)) {
            binding.exportDownloadBtn.visibility = if (hidden) View.GONE else View.VISIBLE
        }
    }

    private fun onShareClicked() {
        val context = context ?: return
        setLoadingState(true)
        fileHelper?.shareRouteFiles(
            context,
            gpxIds.value,
            getSelectedExportFormat(),
            binding.filenameCheckbox.isChecked
        )?.subscribe {
            setLoadingState(false)
            dismiss()
        }.also { it?.let { compositeDisposable.add(it) }}
    }

    private fun onSaveClicked() {
        showSystemFolderPicker()
    }

    private fun showSystemFolderPicker() {
        val gpxId = gpxIds.value.firstOrNull() ?: return
        val filename = fileHelper?.getRouteFilename(
            gpxId,
            getSelectedExportFormat(),
            binding.filenameCheckbox.isChecked
        ) ?: return

        saveFileLauncher.launch(filename)
    }

    private fun onDownloadClicked() {
        saveToDownloadsFolder()
    }

    private fun saveToDownloadsFolder() {
        val context = context ?: return
        setLoadingState(true)
        fileHelper?.apply {
            compositeDisposable.add(
                saveRouteFilesToDownloads(
                    context,
                    gpxIds.value,
                    getSelectedExportFormat(),
                    binding.filenameCheckbox.isChecked
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

    private fun onSaveLocationSelected(location: Uri?) {
        val destination = location ?: return
        val context = context ?: return
        val gpxId = gpxIds.value.firstOrNull() ?: return
        setLoadingState(true)
        fileHelper?.apply {
            compositeDisposable.add(
                saveRouteFile(
                    context,
                    gpxId,
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

    class SaveFileContract : ActivityResultContract<String, Uri?>() {
        override fun createIntent(context: Context, input: String): Intent {
            return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/octet-stream"

                putExtra(Intent.EXTRA_TITLE, input)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return if (resultCode == Activity.RESULT_OK) intent?.data else null
        }
    }

    companion object {
        fun newInstance(gpxIds: List<Long>, actions: List<Action>): ExportFragment {
            val args = Bundle()
            args.putLongArray(Keys.GpxId, gpxIds.toLongArray())
            args.putBoolean("canSave", actions.contains(Action.Save))
            args.putBoolean("canShare", actions.contains(Action.Share))
            args.putBoolean("canDownload", actions.contains(Action.Download))
            val fragment = ExportFragment()
            fragment.arguments = args

            return fragment
        }
    }
}