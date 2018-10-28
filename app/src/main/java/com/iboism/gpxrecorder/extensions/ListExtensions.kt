package com.iboism.gpxrecorder.extensions

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

fun <E> List<E>.takeGist(count: Int): List<E> {
    if (size <= count || size == 0) {
        return this
    } else {
        return List(count) { index ->
            val frac = (index.toFloat() / (count-1).toFloat())
            val cv = (size - 1).toFloat() * frac
            return@List this[cv.toInt()]
        }
    }
}
