package com.android.jerry.ramayanabakery.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Room
import android.content.Context
import com.android.jerry.ramayanabakery.database.daos.CartDao
import com.android.jerry.ramayanabakery.database.entities.Cart

/**
 * Created by riteshksingh on 1/3/18.
 */

@Database(entities = arrayOf(Cart::class), version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cartDao(): CartDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase::class.java, "app.db")
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}