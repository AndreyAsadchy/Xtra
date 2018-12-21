package com.github.exact7.xtra

import android.content.Context
import android.os.Build
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.github.exact7.xtra.util.TlsSocketFactory
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.io.InputStream
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


@GlideModule
class XtraGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)

        /***
         * Enable TLS 1.2 on pre-lollipop devices
         * https://github.com/square/okhttp/issues/2372#issuecomment-244807676
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {
                val builder = OkHttpClient.Builder()
                val sc = SSLContext.getInstance("TLSv1.2")
                val trustManagers = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }

                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                    }
                })
                sc.init(null, trustManagers, SecureRandom())
                builder.sslSocketFactory(TlsSocketFactory(sc.socketFactory), trustManagers[0] as X509TrustManager)
                val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build()
                val specs = ArrayList<ConnectionSpec>().apply {
                    add(cs)
                    add(ConnectionSpec.COMPATIBLE_TLS)
                    add(ConnectionSpec.CLEARTEXT)
                }
                builder.connectionSpecs(specs)
                registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(builder.build()))
            } catch (e: Exception) {
                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", e)
            }
        }
    }
}