package com.android.jerry.ramayanabakery.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Room
import android.content.Context
import com.android.jerry.ramayanabakery.database.daos.WeatherDataDao
import com.android.jerry.ramayanabakery.database.entities.WeatherData

/**
 * Created by riteshksingh on 1/3/18.
 */

@Database(entities = arrayOf(WeatherData::class), version = 1)
abstract class WeatherDataBase : RoomDatabase() {

    abstract fun weatherDataDao(): WeatherDataDao

    companion object {
        private var INSTANCE: WeatherDataBase? = null

        fun getInstance(context: Context): WeatherDataBase? {
            if (INSTANCE == null) {
                synchronized(WeatherDataBase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WeatherDataBase::class.java, "weather.db")
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