package com.iboism.gpxrecorder.model

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import java.nio.ByteBuffer

// God-tier documentation https://bumptech.github.io/glide/tut/custom-modelloader.html
class GpxPreviewLoader: ModelLoader<Long, ByteBuffer> {
    override fun buildLoadData(model: Long, width: Int, height: Int, options: Options): ModelLoader.LoadData<ByteBuffer>? {
        return ModelLoader.LoadData(ObjectKey(model), GpxPreviewDataFetcher(model, width, height))
    }

    override fun handles(model: Long): Boolean {
        return true
    }
}