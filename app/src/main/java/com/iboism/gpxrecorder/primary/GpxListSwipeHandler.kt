package com.iboism.gpxrecorder.primary

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

class GpxListSwipeHandler(private val onCellDismissed: ((position: Int) -> Unit)): ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        (viewHolder as? GpxRecyclerViewAdapter.GpxViewHolder)?.let { onCellDismissed(it.adapterPosition)}
    }
}

//class GpxListSwipeHandler(private val onCellDismissed: (() -> Unit)): View.OnTouchListener {
//    private var startX = 0f
//    private var startY = 0f
//    private var deltaX = 0f
//    private var deltaY = 0f
//    private var currentTouch = false
//
//    override fun onTouch(v: View, event: MotionEvent): Boolean {
//        when(event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                startX = event.rawX
//                startY = event.rawY
//                return false
//            }
//            MotionEvent.ACTION_UP -> {
//                if (currentTouch && (deltaX.absoluteValue < 6 || deltaY.absoluteValue < 6)) {
//                    cancelSwipe(v)
//                    return false
//                }
//
//                if (currentTouch && deltaX.absoluteValue > v.width / 3) {
//                    v.animate()
//                            .translationXBy(deltaX * 3f)
//                            .withEndAction {
//                                onCellDismissed()
//                            }.start()
//
//                    return true
//                }
//
//                return cancelSwipe(v)
//            }
//            MotionEvent.ACTION_MOVE -> {
//                deltaX = (event.rawX - startX)
//                deltaY = (event.rawY - startY)
//                val touchAngle = atan2(deltaY.absoluteValue, deltaX.absoluteValue) * (180 / PI)
//                val horizontalSwipe = touchAngle in -1f..30f
//
//                if (!currentTouch && horizontalSwipe) {
//                    currentTouch = true
//                    startX = event.rawX
//                    return true
//                }
//
//                if (currentTouch) {
//                    v.parent.requestDisallowInterceptTouchEvent(true)
//
//                    v.x = deltaX
//                    v.alpha = (v.width - (deltaX.absoluteValue * 1.3f)) / v.width
//                    return true
//                }
//
//                return false
//            }
//            MotionEvent.ACTION_CANCEL -> {
//                return cancelSwipe(v)
//            }
//            MotionEvent.ACTION_SCROLL -> {
//                return cancelSwipe(v)
//            }
//            else -> {
//                return false
//            }
//        }
//    }
//
//    private fun cancelSwipe(v: View): Boolean {
//        if (currentTouch) {
//            v.parent.requestDisallowInterceptTouchEvent(false)
//            currentTouch = false
//            deltaX = 0f
//            deltaY = 0f
//            v.animate()
//                    .x(0f)
//                    .alpha(1f)
//            return true
//        }
//
//        return true
//    }
//}