package com.iboism.gpxrecorder.records.list

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class GpxListSwipeHandler(private val onCellDismissed: ((position: Int) -> Unit)): ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return when(viewHolder) {
            is GpxContentViewHolder -> super.getSwipeDirs(recyclerView, viewHolder)
            else -> 0
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        (viewHolder as? GpxContentViewHolder)?.let { onCellDismissed(it.adapterPosition) }
    }
}