package com.github.exact7.xtra.di

import android.app.Application
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import com.github.exact7.xtra.BuildConfig
import com.github.exact7.xtra.api.ApiService
import com.github.exact7.xtra.api.BttvApi
import com.github.exact7.xtra.api.IdApi
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.api.MiscApi
import com.github.exact7.xtra.api.UsherApi
import com.github.exact7.xtra.model.chat.SubscriberBadgeDeserializer
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.model.kraken.user.UserEmotesDeserializer
import com.github.exact7.xtra.model.kraken.user.UserEmotesResponse
import com.github.exact7.xtra.repository.KrakenRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.util.FetchProvider
import com.github.exact7.xtra.util.TlsSocketFactory
import com.github.exact7.xtra.util.TwitchApiHelper
import com.google.gson.GsonBuilder
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import dagger.Module
import dagger.Provides
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.Executor
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
class XtraModule {

    @Singleton
    @Provides
    fun providesTwitchService(repository: KrakenRepository): TwitchService {
        return repository
    }

    @Singleton
    @Provides
    fun providesKrakenApi(@Named("okHttpWithClientId") client: OkHttpClient, gsonConverterFactory: GsonConverterFactory, rxJavaAdapterFactory: RxJava2CallAdapterFactory): KrakenApi {
        return Retrofit.Builder()
                .baseUrl("https://api.twitch.tv/kraken/")
                .client(client.newBuilder().addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                            .addHeader("Accept", "application/vnd.twitchtv.v5+json")
                            .build()
                    chain.proceed(request)
                }.build())
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJavaAdapterFactory)
                .build()
                .create(KrakenApi::class.java)
    }

    @Singleton
    @Provides
    fun providesApiService(@Named("okHttpWithClientId") client: OkHttpClient, gsonConverterFactory: GsonConverterFactory, rxJavaAdapterFactory: RxJava2CallAdapterFactory): ApiService {
        return Retrofit.Builder()
                .baseUrl("https://api.twitch.tv/api/")
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJavaAdapterFactory)
                .build()
                .create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun providesUsherApi(@Named("okHttpWithClientId") client: OkHttpClient, gsonConverterFactory: GsonConverterFactory, rxJavaAdapterFactory: RxJava2CallAdapterFactory): UsherApi {
        return Retrofit.Builder()
                .baseUrl("https://usher.ttvnw.net/")
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJavaAdapterFactory)
                .build()
                .create(UsherApi::class.java)
    }

    @Singleton
    @Provides
    fun providesMiscApi(@Named("okHttpDefault") client: OkHttpClient, gsonConverterFactory: GsonConverterFactory, rxJavaAdapterFactory: RxJava2CallAdapterFactory): MiscApi {
        return Retrofit.Builder()
                .baseUrl("https://api.twitch.tv/") //placeholder url
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJavaAdapterFactory)
                .build()
                .create(MiscApi::class.java)
    }

    @Singleton
    @Provides
    fun providesIdApi(@Named("okHttpDefault") client: OkHttpClient, gsonConverterFactory: GsonConverterFactory, rxJavaAdapterFactory: RxJava2CallAdapterFactory): IdApi {
        return Retrofit.Builder()
                .baseUrl("https://id.twitch.tv/oauth2/")
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJavaAdapterFactory)
                .build()
                .create(IdApi::class.java)
    }

    @Singleton
    @Provides
    fun providesBttvApi(@Named("okHttpDefault") client: OkHttpClient, gsonConverterFactory: GsonConverterFactory, rxJavaAdapterFactory: RxJava2CallAdapterFactory): BttvApi {
        return Retrofit.Builder()
                .baseUrl("https://api.betterttv.net/2/")
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJavaAdapterFactory)
                .build()
                .create(BttvApi::class.java)
    }

    @Singleton
    @Provides
    fun providesGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .registerTypeAdapter(SubscriberBadgesResponse::class.java, SubscriberBadgeDeserializer())
                .registerTypeAdapter(UserEmotesResponse::class.java, UserEmotesDeserializer())
                .create())
    }

    @Singleton
    @Provides
    fun providesRxJavaCallAdapterFactory(): RxJava2CallAdapterFactory {
        return RxJava2CallAdapterFactory.create()
    }

    @Singleton
    @Provides
    @Named("okHttpDefault")
    fun providesDefaultOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            }

            /***
             * Enable TLS 1.2 on pre-lollipop devices
             * https://github.com/square/okhttp/issues/2372#issuecomment-244807676
             */
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                try {
                    val sc = SSLContext.getInstance("TLSv1.2")
                    val trustManagers = arrayOf<TrustManager>(object : X509TrustManager {
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                        }

                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                        }
                    })
                    sc.init(null, trustManagers, SecureRandom())
                    sslSocketFactory(TlsSocketFactory(sc.socketFactory), trustManagers[0] as X509TrustManager)
                    val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                            .tlsVersions(TlsVersion.TLS_1_2)
                            .build()
                    val specs = ArrayList<ConnectionSpec>().apply {
                        add(cs)
                        add(ConnectionSpec.COMPATIBLE_TLS)
                        add(ConnectionSpec.CLEARTEXT)
                    }
                    connectionSpecs(specs)
                } catch (e: Exception) {
                    Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", e)
                }
            }
        }
        return builder.build()
    }

    @Singleton
    @Provides
    @Named("okHttpWithClientId")
    fun providesOkHttpClientWithClientId(@Named("okHttpDefault") okHttpClient: OkHttpClient): OkHttpClient {
        return okHttpClient.newBuilder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
                    .addHeader("Client-ID", TwitchApiHelper.clientId)
                    .build()
            chain.proceed(request)
        }.build()
    }

    @Singleton
    @Provides
    fun providesExecutor(): Executor {
        return AsyncTask.THREAD_POOL_EXECUTOR
    }

    @Singleton
    @Provides
    fun providesFetchProvider(fetchConfiguration: FetchConfiguration): FetchProvider {
        return FetchProvider(fetchConfiguration)
    }

    @Singleton
    @Provides
    fun providesFetchConfiguration(application: Application, @Named("okHttpDefault") okHttpClient: OkHttpClient): FetchConfiguration {
        return FetchConfiguration.Builder(application)
                .enableLogging(BuildConfig.DEBUG)
                .enableRetryOnNetworkGain(true)
                .setDownloadConcurrentLimit(3)
                .setHttpDownloader(OkHttpDownloader(okHttpClient, Downloader.FileDownloaderType.PARALLEL))
                .setProgressReportingInterval(1000L)
                .build()
    }
}
