package com.android.jerry.ramayanabakery.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Room
import android.content.Context
import com.android.jerry.ramayanabakery.database.daos.BookingDao
import com.android.jerry.ramayanabakery.database.daos.CartDao
import com.android.jerry.ramayanabakery.database.entities.Booking
import com.android.jerry.ramayanabakery.database.entities.Cart


@Database(entities = arrayOf(Cart::class,Booking::class), version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cartDao(): CartDao
    abstract fun bookingDao(): BookingDao

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