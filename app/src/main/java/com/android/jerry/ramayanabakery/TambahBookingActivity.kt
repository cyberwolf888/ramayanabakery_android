package com.android.jerry.ramayanabakery

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.android.jerry.ramayanabakery.database.AppDatabase
import com.android.jerry.ramayanabakery.database.entities.Booking
import com.android.jerry.ramayanabakery.utility.DbWorkerThread
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.android.jerry.ramayanabakery.utility.Session
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_tambah_booking.*

class TambahBookingActivity : AppCompatActivity() {

    private var id_toko: String? = null
    private var id_produk: String? = null
    private var nama_toko: String? = null
    private var nama_produk: String? = null
    private var harga: String? = null
    internal var session: Session? = null

    private var mDb: AppDatabase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    companion object {

        private val REQ_TOKO = 111
        private val REQ_PRODUK = 222
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        session = Session(this@TambahBookingActivity)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_booking)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        mDb = AppDatabase.getInstance(this)

        toko.setOnClickListener{
            val i = Intent(this@TambahBookingActivity, ListTokoActivity::class.java)
            startActivityForResult(i, REQ_TOKO)
        }

        produk.setOnClickListener{
            val i = Intent(this@TambahBookingActivity, ListProdukActivity::class.java)
            startActivityForResult(i, REQ_PRODUK)
        }

        btnNext.setOnClickListener {
            simpan()
        }
    }

    fun simpan(){
        qty.error = null

        val qtyStr = qty.text.toString()

        var cancel = false
        var focusView: View? = null

        if (TextUtils.isEmpty(qtyStr)) {
            qty.error = "Qty tidak boleh kosong."
            focusView = qty
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            var cartData = Booking()
            cartData.toko_id = id_toko ?: ""
            cartData.barang_id = id_produk ?: ""
            cartData.qty = qtyStr
            cartData.nama_toko = nama_toko ?: ""
            cartData.nama_produk = nama_produk ?: ""
            cartData.harga = harga ?: ""

            insertCart(cartData = cartData)
        }


    }

    private fun insertCart(cartData: Booking) {
        val task = Runnable {
            val result = mDb?.bookingDao()?.checkProduct(cartData.toko_id,cartData.barang_id)

            mUiHandler.post({
                if (result == null || result?.size == 0) {
                    val task2 = Runnable {
                        mDb?.bookingDao()?.insert(cartData)
                        mUiHandler.post({
                            val i = Intent(this@TambahBookingActivity, KeranjangBookingActivity::class.java)
                            startActivity(i)
                            finish()

                        })
                    }
                    mDbWorkerThread.postTask(task2)
                }else{
                    val cart = result.get(0)
                    cart.qty = (cartData.qty.toInt() + cart.qty.toInt()).toString()

                    val task3 = Runnable {
                        mDb?.bookingDao()?.update(cart)
                        mUiHandler.post({
                            val i = Intent(this@TambahBookingActivity, KeranjangBookingActivity::class.java)
                            startActivity(i)
                            finish()
                        })
                    }
                    mDbWorkerThread.postTask(task3)
                }

            })
        }
        mDbWorkerThread.postTask(task)

    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Batal")
                .setMessage("Data penjualan anda akan hilang jika kembali saat ini")
                .setPositiveButton("Iya") { dialog, which -> finish() }
                .setNegativeButton("Tidak", null)
                .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            android.R.id.home -> {
                AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Batal")
                        .setMessage("Data penjualan anda akan hilang jika kembali saat ini")
                        .setPositiveButton("Iya") { dialog, which -> finish() }
                        .setNegativeButton("Tidak", null)
                        .show()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_TOKO && resultCode == Activity.RESULT_OK && data != null) {
            id_toko = data.getStringExtra("id_toko")
            nama_toko = data.getStringExtra("nama_toko")
            toko.setText(nama_toko)
        }
        if (requestCode == REQ_PRODUK && resultCode == Activity.RESULT_OK && data != null) {
            id_produk = data.getStringExtra("id_produk")
            nama_produk = data.getStringExtra("nama_produk")
            harga = data.getStringExtra("harga")
            produk.setText(nama_produk)
        }
    }
}
