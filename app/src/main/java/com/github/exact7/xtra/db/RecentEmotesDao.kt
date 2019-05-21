package com.github.exact7.xtra.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.exact7.xtra.model.chat.RecentEmote


@Dao
interface RecentEmotesDao {

    @Query("SELECT * FROM recent_emotes ORDER BY used_at DESC")
    fun getAll(): LiveData<List<RecentEmote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(emotes: Collection<RecentEmote>)

    @Query("DELETE FROM recent_emotes WHERE name IN (SELECT name FROM recent_emotes LIMIT :count)")
    fun delete(count: Int)

    @Query("SELECT COUNT(*) FROM recent_emotes")
    fun getSize(): Int

    @Transaction
    fun ensureMaxSizeAndInsert(emotes: Collection<RecentEmote>) {
        val deleteCount = getSize() + emotes.size - RecentEmote.MAX_SIZE
        if (deleteCount > 0) {
            delete(deleteCount)
        }
        insertAll(emotes)
    }
}
