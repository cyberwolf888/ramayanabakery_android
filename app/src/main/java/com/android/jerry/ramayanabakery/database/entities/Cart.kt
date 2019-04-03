package com.android.jerry.ramayanabakery.database.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey


/**
 *  Represents a table within the database.
 */
@Entity(tableName = "cart")
data class Cart(@PrimaryKey(autoGenerate = true) var cart_id: Long?,
                       @ColumnInfo(name = "toko_id") var toko_id: String,
                       @ColumnInfo(name = "barang_id") var barang_id: String,
                       @ColumnInfo(name = "qty") var qty: String,
                       @ColumnInfo(name = "harga") var harga: String,
                       @ColumnInfo(name = "nama_toko") var nama_toko: String,
                       @ColumnInfo(name = "nama_produk") var nama_produk: String


){
    constructor():this(null,"","","","","", "")
}