package com.github.exact7.xtra

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.github.exact7.xtra.db.AppDatabase
import com.github.exact7.xtra.util.TwitchApiHelper
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
    }