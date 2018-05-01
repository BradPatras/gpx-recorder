package com.iboism.gpxrecorder.util

/**
 * Created by bradpatras on 4/30/18.
 */

/**
 * If the list is longer than [count] then return a sublist of the first [count] elements,
 * otherwise return the full list
 */
fun <E> List<E>.takeFirst(count: Int): List<E> {
    return if (this.size <= count) this else this.subList(0, count)
}
