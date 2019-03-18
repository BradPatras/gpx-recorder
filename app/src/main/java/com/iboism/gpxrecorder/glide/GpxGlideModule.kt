package com.iboism.gpxrecorder.glide

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.signature.ObjectKey
import com.iboism.gpxrecorder.model.GpxPreviewDataFetcher

@GlideModule
class GpxGlideModule: AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(GpxModelKey::class.java, Bitmap::class.java, GpxPreviewLoaderFactory())
    }
}

class GpxModelKey(val key: Long): Comparable<GpxModelKey> {
    override fun compareTo(other: GpxModelKey): Int {
        return this.key.compareTo(other.key)
    }
}

// God-tier documentation https://bumptech.github.io/glide/tut/custom-modelloader.html
class GpxPreviewLoader: ModelLoader<GpxModelKey, Bitmap> {
    override fun buildLoadData(model: GpxModelKey, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap>? {
        return ModelLoader.LoadData(ObjectKey(model), GpxPreviewDataFetcher(model.key, width, height))
    }

    override fun handles(model: GpxModelKey): Boolean {
        return true
    }
}

class GpxPreviewLoaderFactory: ModelLoaderFactory<GpxModelKey, Bitmap> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<GpxModelKey, Bitmap> {
        return GpxPreviewLoader()
    }

    override fun teardown() {}
}