package com.walltext.app

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 一条想法（一句要上墙的文字）。
 */
@Entity(tableName = "thoughts")
data class Thought(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val styleKey: String,        // TextStyle 的 key（枚举名）
    val timestamp: Long = System.currentTimeMillis()
)