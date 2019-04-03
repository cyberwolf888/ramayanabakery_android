package com.android.jerry.ramayanabakery.database.daos


import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.android.jerry.ramayanabakery.database.entities.Cart

/**
 * Created by riteshksingh on 1/2/18.
 */
@Dao
interface CartDao {

    @Query("SELECT * from cart")
    fun getAll(): List<Cart>

    @Query("SELECT * from cart ORDER By toko_id")
    fun getOrderByToko(): List<Cart>

    @Query("SELECT * from cart WHERE toko_id = :tokoID AND barang_id = :barangId")
    fun checkProduct(tokoID: String, barangId: String): List<Cart>

    @Insert(onConflict = REPLACE)
    fun insert(cart: Cart)

    @Update
    fun update(cart: Cart)


    @Query("DELETE from cart")
    fun deleteAll()

    @Delete
    fun deleteCart(cart: Cart)

}