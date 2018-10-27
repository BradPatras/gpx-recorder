package com.iboism.gpxrecorder.records.list

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

class GpxListSwipeHandler(private val onCellDismissed: ((position: Int) -> Unit)): ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        (viewHolder as? GpxViewHolder)?.let { onCellDismissed(it.adapterPosition) }
    }
}