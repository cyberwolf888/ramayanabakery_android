package com.android.jerry.ramayanabakery.database.daos


import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.android.jerry.ramayanabakery.database.entities.Booking

/**
 * Created by riteshksingh on 1/2/18.
 */
@Dao
interface BookingDao {

    @Query("SELECT * from booking")
    fun getAll(): List<Booking>

    @Query("SELECT * from booking ORDER By toko_id")
    fun getOrderByToko(): List<Booking>

    @Query("SELECT * from booking WHERE toko_id = :tokoID AND barang_id = :barangId")
    fun checkProduct(tokoID: String, barangId: String): List<Booking>

    @Insert(onConflict = REPLACE)
    fun insert(booking: Booking)

    @Update
    fun update(booking: Booking)


    @Query("DELETE from booking")
    fun deleteAll()

    @Delete
    fun deleteCart(booking: Booking)

}