package com.dulumina.androtik.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RouterProfileEntity::class], version = 1, exportSchema = false)
abstract class AndrotikDatabase : RoomDatabase() {

    abstract fun routerProfileDao(): RouterProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AndrotikDatabase? = null

        fun getInstance(context: Context): AndrotikDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AndrotikDatabase::class.java,
                    "androtik_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
