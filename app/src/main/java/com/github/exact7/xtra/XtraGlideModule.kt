package com.github.exact7.xtra

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.github.exact7.xtra.di.AppInjector
import okhttp3.OkHttpClient
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Named


@GlideModule
class XtraGlideModule : AppGlideModule() {

    @field:[Inject Named("okHttpDefault")]
    lateinit var okHttpClient: OkHttpClient

    init {
        AppInjector.daggerComponent.inject(this)
    }

    override fun isManifestParsingEnabled(): Boolean = false

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(okHttpClient))
        super.registerComponents(context, glide, registry)
    }
}
