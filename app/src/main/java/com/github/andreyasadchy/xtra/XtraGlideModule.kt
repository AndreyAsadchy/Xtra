package com.github.andreyasadchy.xtra

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.annotation.GlideExtension
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.annotation.GlideType
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.github.andreyasadchy.xtra.di.AppInjector
import okhttp3.OkHttpClient
import java.io.InputStream
import javax.inject.Inject


@GlideModule
class XtraGlideModule : AppGlideModule() {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    init {
        AppInjector.daggerComponent.inject(this)
    }

    override fun isManifestParsingEnabled(): Boolean = false

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(okHttpClient))
        super.registerComponents(context, glide, registry)
    }

    @GlideExtension
    companion object XtraGlideExtension {
        private val DECODE_TYPE_WEBP: RequestOptions = GlideOptions.decodeTypeOf(WebpDrawable::class.java).lock()
        @GlideType(WebpDrawable::class)
        fun asWebp(requestBuilder: RequestBuilder<WebpDrawable?>): RequestBuilder<WebpDrawable?> {
            return requestBuilder
                .transition(DrawableTransitionOptions())
                .apply(DECODE_TYPE_WEBP)
        }
    }
}
