package com.github.exact7.xtra.di

import android.app.Application
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.exact7.xtra.db.AppDatabase
import com.github.exact7.xtra.db.VideosDao
import com.github.exact7.xtra.repository.OfflineRepository
import com.github.exact7.xtra.util.TwitchApiHelper
import dagger.Module
import dagger.Provides
import java.util.Calendar
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun providesAppDatabase(application: Application): AppDatabase =
            Room.databaseBuilder(application, AppDatabase::class.java, "database")
                    .addMigrations(object : Migration(1, 2) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("CREATE TABLE videos1 (id INTEGER NOT NULL, url TEXT NOT NULL, name TEXT NOT NULL, channel_name TEXT NOT NULL, channel_logo TEXT NOT NULL, thumbnail TEXT NOT NULL, game TEXT NOT NULL, duration INTEGER NOT NULL, upload_date INTEGER NOT NULL, download_date INTEGER NOT NULL, is_vod INTEGER NOT NULL, downloaded INTEGER NOT NULL, PRIMARY KEY (id))")
                            val cursor = database.query("SELECT * FROM videos")
                            while (cursor.moveToNext()) {
                                val values = ContentValues().apply {
                                    put("id", cursor.getInt(0))
                                    put("url", cursor.getString(2))
                                    put("name", cursor.getString(3))
                                    put("channel_name", cursor.getString(4))
                                    put("channel_logo", cursor.getString(10))
                                    put("thumbnail", cursor.getString(9))
                                    put("game", cursor.getString(5))
                                    put("duration", cursor.getLong(6))
                                    put("upload_date", TwitchApiHelper.parseIso8601Date(cursor.getString(8)))
                                    put("download_date", Calendar.getInstance().time.time)
                                    put("is_vod", cursor.getInt(1))
                                    put("downloaded", 1)
                                }
                                database.insert("videos1", SQLiteDatabase.CONFLICT_NONE, values)
                            }
                            database.execSQL("DROP TABLE videos")
                            database.execSQL("ALTER TABLE videos1 RENAME TO videos")
                        }
                    })
                    .build()
//                            object : Migration(2, 3) {
//                                override fun migrate(database: SupportSQLiteDatabase) {
//                                    database.execSQL("CREATE TABLE videos1 (id INTEGER NOT NULL, url TEXT NOT NULL, source_url TEXT NOT NULL, name TEXT NOT NULL, channel_name TEXT NOT NULL, channel_logo TEXT NOT NULL, thumbnail TEXT NOT NULL, game TEXT NOT NULL, duration INTEGER NOT NULL, upload_date INTEGER NOT NULL, download_date INTEGER NOT NULL, is_vod INTEGER NOT NULL, downloaded INTEGER NOT NULL, PRIMARY KEY (id))")
//                                    val cursor = database.query("SELECT * FROM videos")
//                                    while (cursor.moveToNext()) {
//                                        val values = ContentValues().apply {
//                                            put("id", cursor.getInt(0))
//                                            put("source_url", "")
//                                            put("url", cursor.getString(2))
//                                            put("name", cursor.getString(3))
//                                            put("channel_name", cursor.getString(4))
//                                            put("channel_logo", cursor.getString(5))
//                                            put("thumbnail", cursor.getString(6))
//                                            put("game", cursor.getString(7))
//                                            put("duration", cursor.getLong(8))
//                                            put("upload_date", cursor.getString(9))
//                                            put("download_date", cursor.getLong(10))
//                                            put("is_vod", cursor.getInt(11))
//                                            put("downloaded", cursor.getInt(1))
//                                        }
//                                        database.insert("videos1", SQLiteDatabase.CONFLICT_NONE, values)
//                                    }
//                                    database.execSQL("DROP TABLE videos")
//                                    database.execSQL("ALTER TABLE videos1 RENAME TO videos")
//                                }
//                            })

    @Singleton
    @Provides
    fun providesVideosDao(database: AppDatabase): VideosDao = database.videos()

    @Singleton
    @Provides
    fun providesRepository(videosDao: VideosDao): OfflineRepository = OfflineRepository(videosDao)
}
