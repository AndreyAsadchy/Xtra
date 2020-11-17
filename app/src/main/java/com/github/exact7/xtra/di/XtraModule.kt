package com.github.exact7.xtra.di

import android.app.Application
import android.os.Build
import android.util.Log
import com.github.exact7.xtra.BuildConfig
import com.github.exact7.xtra.api.ApiService
import com.github.exact7.xtra.api.GraphQLApi
import com.github.exact7.xtra.api.IdApi
import com.github.exact7.xtra.api.KrakenApi
import com.github.exact7.xtra.api.MiscApi
import com.github.exact7.xtra.api.UsherApi
import com.github.exact7.xtra.model.chat.FfzEmotesResponse
import com.github.exact7.xtra.model.chat.FfzRoomDeserializer
import com.github.exact7.xtra.model.chat.SubscriberBadgeDeserializer
import com.github.exact7.xtra.model.chat.SubscriberBadgesResponse
import com.github.exact7.xtra.model.gql.clip.ClipDataDeserializer
import com.github.exact7.xtra.model.gql.clip.ClipDataResponse
import com.github.exact7.xtra.model.kraken.user.UserEmotesDeserializer
import com.github.exact7.xtra.model.kraken.user.UserEmotesResponse
import com.github.exact7.xtra.repository.KrakenRepository
import com.github.exact7.xtra.repository.TwitchService
import com.github.exact7.xtra.util.FetchProvider
import com.github.exact7.xtra.util.RemoteConfigParams
import com.github.exact7.xtra.util.TlsSocketFactory
import com.github.exact7.xtra.util.TwitchApiHelper
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.gson.GsonBuilder
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import dagger.Module
import dagger.Provides
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
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
    fun providesKrakenApi(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): KrakenApi {
        return Retrofit.Builder()
                .baseUrl("https://api.twitch.tv/kraken/")
                .client(client.newBuilder().addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                            .addHeader("Client-ID", TwitchApiHelper.CLIENT_ID)
                            .addHeader("Accept", "application/vnd.twitchtv.v5+json")
                            .build()
                    chain.proceed(request)
                }.build())
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(KrakenApi::class.java)
    }

    @Singleton
    @Provides
    fun providesApiService(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): ApiService {
        return Retrofit.Builder()
                .baseUrl("https://api.twitch.tv/api/")
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(ApiService::class.java)
    }

    @Singleton
    @Provides
    fun providesUsherApi(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): UsherApi {
        return Retrofit.Builder()
                .baseUrl("https://usher.ttvnw.net/")
                .client(client.newBuilder().addInterceptor { chain ->
                    val builder = chain.request().newBuilder()
                    val userAgent = Firebase.remoteConfig.getString(RemoteConfigParams.TWITCH_PLAYER_USER_AGENT_KEY)
                    if (userAgent != RemoteConfigParams.TWITCH_PLAYER_USER_AGENT_DEFAULT) {
                        builder.addHeader("User-Agent", userAgent)
                    }
                    chain.proceed(builder.build())
                }.build())
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(UsherApi::class.java)
    }

    @Singleton
    @Provides
    fun providesMiscApi(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): MiscApi {
        return Retrofit.Builder()
                .baseUrl("https://api.twitch.tv/") //placeholder url
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(MiscApi::class.java)
    }

    @Singleton
    @Provides
    fun providesIdApi(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): IdApi {
        return Retrofit.Builder()
                .baseUrl("https://id.twitch.tv/oauth2/")
                .client(client)
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(IdApi::class.java)
    }

    @Singleton
    @Provides
    fun providesGraphQLApi(client: OkHttpClient, gsonConverterFactory: GsonConverterFactory): GraphQLApi {
        return Retrofit.Builder()
                .baseUrl("https://gql.twitch.tv/gql/")
                .client(client.newBuilder().addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                            .addHeader("Client-ID", TwitchApiHelper.TWITCH_CLIENT_ID)
                            .build()
                    chain.proceed(request)
                }.build())
                .addConverterFactory(gsonConverterFactory)
                .build()
                .create(GraphQLApi::class.java)
    }

    @Singleton
    @Provides
    fun providesGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .registerTypeAdapter(SubscriberBadgesResponse::class.java, SubscriberBadgeDeserializer())
                .registerTypeAdapter(UserEmotesResponse::class.java, UserEmotesDeserializer())
                .registerTypeAdapter(FfzEmotesResponse::class.java, FfzRoomDeserializer())
                .registerTypeAdapter(ClipDataResponse::class.java, ClipDataDeserializer())
                .create())
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    val trustManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).run {
                        init(null as KeyStore?)
                        trustManagers.first { it is X509TrustManager } as X509TrustManager
                    }
                    val sslContext = SSLContext.getInstance(TlsVersion.TLS_1_2.javaName())
                    sslContext.init(null, arrayOf(trustManager), null)
                    val cipherSuites = ConnectionSpec.MODERN_TLS.cipherSuites()!!.toMutableList().apply {
                        add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA)
                    }.toTypedArray()
                    sslSocketFactory(TlsSocketFactory(sslContext.socketFactory), trustManager)
                    val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                            .cipherSuites(*cipherSuites)
                            .build()
                    connectionSpecs(arrayListOf(cs))
                } catch (e: Exception) {
                    Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2 compatibility", e)
                }
            }
            connectTimeout(5, TimeUnit.MINUTES)
            writeTimeout(5, TimeUnit.MINUTES)
            readTimeout(5, TimeUnit.MINUTES)
        }
        return builder.build()
    }

    @Singleton
    @Provides
    fun providesFetchProvider(fetchConfigurationBuilder: FetchConfiguration.Builder): FetchProvider {
        return FetchProvider(fetchConfigurationBuilder)
    }

    @Singleton
    @Provides
    fun providesFetchConfigurationBuilder(application: Application, okHttpClient: OkHttpClient): FetchConfiguration.Builder {
        return FetchConfiguration.Builder(application)
                .enableLogging(BuildConfig.DEBUG)
                .enableRetryOnNetworkGain(true)
                .setDownloadConcurrentLimit(3)
                .setHttpDownloader(OkHttpDownloader(okHttpClient))
                .setProgressReportingInterval(1000L)
                .setAutoRetryMaxAttempts(3)
    }
}
