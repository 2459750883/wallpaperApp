package com.walltext.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.walltext.app.Thought

@Dao
interface ThoughtDao {

    @Insert
    suspend fun insert(thought: Thought): Long

    @Query("SELECT * FROM thoughts ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecent(): List<Thought>

    @Query("SELECT * FROM thoughts ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): Thought?

    @Query("DELETE FROM thoughts WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 插入完成后清理超过 50 条的最旧记录。
     */
    @Query("""
        DELETE FROM thoughts
        WHERE id NOT IN (
            SELECT id FROM thoughts ORDER BY timestamp DESC LIMIT 50
        )
    """)
    suspend fun trimToLimit()
}