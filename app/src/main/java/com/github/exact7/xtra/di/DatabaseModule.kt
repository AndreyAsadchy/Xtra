package com.github.exact7.xtra.di

import android.app.Application
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.exact7.xtra.db.AppDatabase
import com.github.exact7.xtra.db.EmotesDao
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
                    .addMigrations(
                            object : Migration(1, 2) {
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
                            },
                            object : Migration(2, 3) {
                                override fun migrate(database: SupportSQLiteDatabase) {
                                    database.execSQL("CREATE TABLE IF NOT EXISTS emotes (id INTEGER NOT NULL, code TEXT NOT NULL, PRIMARY KEY (id))")
                                }
                            },
                            object : Migration(3, 4) {
                                override fun migrate(database: SupportSQLiteDatabase) {
                                    database.execSQL("CREATE TABLE videos1 (id INTEGER NOT NULL, url TEXT NOT NULL, source_url TEXT NOT NULL, source_start_position INTEGER, source_end_position INTEGER, name TEXT NOT NULL, channel_name TEXT NOT NULL, channel_logo TEXT NOT NULL, thumbnail TEXT NOT NULL, game TEXT NOT NULL, duration INTEGER NOT NULL, upload_date INTEGER NOT NULL, download_date INTEGER NOT NULL, is_vod INTEGER NOT NULL, downloaded INTEGER NOT NULL, last_watch_position INTEGER NOT NULL, PRIMARY KEY (id))")
                                    val cursor = database.query("SELECT * FROM videos")
                                    while (cursor.moveToNext()) {
                                        val values = ContentValues().apply {
                                            put("id", cursor.getInt(0))
                                            put("url", cursor.getString(3))
                                            put("source_url", "")
                                            putNull("source_start_position")
                                            putNull("source_end_position")
                                            put("name", cursor.getString(4))
                                            put("channel_name", cursor.getString(5))
                                            put("channel_logo", cursor.getString(6))
                                            put("thumbnail", cursor.getString(7))
                                            put("game", cursor.getString(8))
                                            put("duration", cursor.getLong(9) * 1000L)
                                            put("upload_date", cursor.getLong(10))
                                            put("download_date", cursor.getLong(11))
                                            put("is_vod", cursor.getInt(1))
                                            put("downloaded", cursor.getInt(2))
                                            put("last_watch_position", 0L)
                                        }
                                        database.insert("videos1", SQLiteDatabase.CONFLICT_NONE, values)
                                    }
                                    database.execSQL("DROP TABLE videos")
                                    database.execSQL("ALTER TABLE videos1 RENAME TO videos")
                                }
                            }
                    )
                    .build()


    @Singleton
    @Provides
    fun providesRepository(videosDao: VideosDao): OfflineRepository = OfflineRepository(videosDao)

    @Singleton
    @Provides
    fun providesVideosDao(database: AppDatabase): VideosDao = database.videos()

    @Singleton
    @Provides
    fun providesEmotesDao(database: AppDatabase): EmotesDao = database.emotes()
}
