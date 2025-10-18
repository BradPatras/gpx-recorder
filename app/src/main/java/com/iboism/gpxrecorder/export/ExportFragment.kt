package com.iboism.gpxrecorder.export

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
    private lateinit var gpxIds: Holder<List<Long>>
    private val fileHelper: FileHelper? by lazy { FileHelper() }
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val compositeDisposable = CompositeDisposable()

    private lateinit var binding: FragmentExportBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        binding = FragmentExportBinding.inflate(inflater, container, container != null)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gpxIds = Holder(requireArguments().getLongArray(Keys.GpxId)?.toList() ?: listOf())
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

    companion object {
        fun newInstance(gpxIds: List<Long>): ExportFragment {
            val args = Bundle()
            args.putLongArray(Keys.GpxId, gpxIds.toLongArray())

            val fragment = ExportFragment()
            fragment.arguments = args

            return fragment
        }
    }
}