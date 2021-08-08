package com.github.andreyasadchy.xtra

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.github.andreyasadchy.xtra.db.AppDatabase
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.Calendar

class MigrationTest {

    private val TEST_DB = "database"

    @get:Rule
    val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory())

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        val migration = object : Migration(1, 2) {
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
        }
        helper.createDatabase(TEST_DB, 1).apply {
            val values = ContentValues().apply {
                put("url", "url")
                put("name", "name")
                put("channel", "channel")
                put("game", "game")
                put("length", 150)
                put("download_date", "download date")
                put("upload_date", "2016-12-15T20:33:02Z")
                put("thumbnail", "thumbnail")
                put("streamerAvatar", "streamer avatar")
                put("is_vod", 1)
            }
            insert("videos", SQLiteDatabase.CONFLICT_NONE, values)
            close()
        }
        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, migration)
        val cursor = db.query("SELECT * FROM videos")
        while (cursor.moveToNext()) {
            for (i in 0 until cursor.columnCount) {
                println("${cursor.getColumnName(i)} ${cursor.getString(i)}")
            }
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        val migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS requests (offline_video_id INTEGER NOT NULL, url TEXT NOT NULL, path TEXT NOT NULL, video_id TEXT, segment_from INTEGER, segment_to INTEGER, PRIMARY KEY (offline_video_id), FOREIGN KEY('offline_video_id') REFERENCES videos('id') ON DELETE CASCADE)")

                database.execSQL("CREATE TABLE videos1 (id INTEGER NOT NULL, url TEXT NOT NULL, source_url TEXT NOT NULL, source_start_position INTEGER, name TEXT NOT NULL, channel_name TEXT NOT NULL, channel_logo TEXT NOT NULL, thumbnail TEXT NOT NULL, game TEXT NOT NULL, duration INTEGER NOT NULL, upload_date INTEGER NOT NULL, download_date INTEGER NOT NULL, is_vod INTEGER NOT NULL, last_watch_position INTEGER NOT NULL, progress INTEGER NOT NULL, max_progress INTEGER NOT NULL, status INTEGER NOT NULL, PRIMARY KEY (id))")

                val cursor = database.query("SELECT * FROM videos")
                while (cursor.moveToNext()) {
                    val values = ContentValues().apply {
                        put("id", cursor.getInt(0))
                        put("url", cursor.getString(4))
                        put("source_url", cursor.getString(5))
                        put("source_start_position", cursor.getLong(6))
                        put("name", cursor.getString(7))
                        put("channel_name", cursor.getString(8))
                        put("channel_logo", cursor.getString(9))
                        put("thumbnail", cursor.getString(10))
                        put("game", cursor.getString(11))
                        put("duration", cursor.getLong(12))
                        put("upload_date", cursor.getLong(13))
                        put("download_date", cursor.getLong(14))
                        put("last_watch_position", cursor.getLong(3))
                        put("progress", 0)
                        put("max_progress", 0)
                        put("status", 2)
                        put("is_vod", cursor.getInt(1))
                    }
                    for (i in 0 until cursor.columnCount) {
                        println("${cursor.getColumnName(i)} ${cursor.getString(i)}")
                    }
                    database.insert("videos1", SQLiteDatabase.CONFLICT_NONE, values)
                }
                println("---------")
                database.execSQL("DROP TABLE videos")
                database.execSQL("ALTER TABLE videos1 RENAME TO videos")
            }
        }
        helper.createDatabase(TEST_DB, 4).apply {
            val values = ContentValues().apply {
                put("url", "url")
                put("source_url", "sourceUrl")
                put("source_start_position", 5000L)
                put("name", "name")
                put("channel_name", "channel_name")
                put("channel_logo", "channel_logo")
                put("thumbnail", "thumbnail")
                put("game", "game")
                put("duration", 20000)
                put("download_date", System.currentTimeMillis())
                put("upload_date", System.currentTimeMillis())
                put("downloaded", 0)
                put("is_vod", 1)
                put("last_watch_position", 0L)
            }
            insert("videos", SQLiteDatabase.CONFLICT_NONE, values)
            close()
        }
        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, migration)
        val cursor = db.query("SELECT * FROM videos")
        while (cursor.moveToNext()) {
            for (i in 0 until cursor.columnCount) {
                println("${cursor.getColumnName(i)} ${cursor.getString(i)}")
            }
        }
        cursor.close()
    }
}