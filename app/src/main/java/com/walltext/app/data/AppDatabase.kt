package com.walltext.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.walltext.app.Thought

@Database(entities = [Thought::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun thoughtDao(): ThoughtDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "walltext.db"
                ).build()
                INSTANCE = db
                db
            }
        }
    }
}