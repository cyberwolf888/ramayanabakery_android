package com.android.jerry.ramayanabakery

import android.app.Activity
import android.content.DialogInterface
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
import com.android.jerry.ramayanabakery.database.WeatherDataBase
import com.android.jerry.ramayanabakery.database.entities.Cart
import com.android.jerry.ramayanabakery.utility.DbWorkerThread
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.android.jerry.ramayanabakery.utility.Session
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_tambah_penjualan.*

class TambahPenjualanActivity : AppCompatActivity() {

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
        session = Session(this@TambahPenjualanActivity)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_penjualan)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        mDb = AppDatabase.getInstance(this@TambahPenjualanActivity)


        toko.setOnClickListener{
            val i = Intent(this@TambahPenjualanActivity, ListTokoActivity::class.java)
            startActivityForResult(i, REQ_TOKO)
        }

        produk.setOnClickListener{
            val i = Intent(this@TambahPenjualanActivity, ListProdukActivity::class.java)
            startActivityForResult(i, REQ_PRODUK)
        }

        btnNext.setOnClickListener {
            simpan();
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
            var cartData = Cart()
            cartData.toko_id = id_toko ?: ""
            cartData.barang_id = id_produk ?: ""
            cartData.qty = qtyStr
            cartData.nama_toko = nama_toko ?: ""
            cartData.nama_produk = nama_produk ?: ""
            cartData.harga = harga ?: ""

            insertCart(cartData = cartData)


            /*
            val url = RequestServer().getServer_url() + "simpan_transaksi.php"
            Log.d("Url", ">" + url)

            val jsonReq = JsonObject()
            jsonReq.addProperty("id_pegawai", session!!.getUserId())
            jsonReq.addProperty("id_product", id_produk)
            jsonReq.addProperty("id_member", id_toko)
            jsonReq.addProperty("qty", qtyStr)

            Log.d("Cek Req", ">" + jsonReq)

            Ion.with(this@TambahPenjualanActivity)
                    .load(url)
                    //.setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
                    .setJsonObjectBody(jsonReq)
                    .asJsonObject()
                    .setCallback { e, result ->
                        Log.d("Response", ">" + result)

                        try {
                            val status = result.get("status").toString()
                            if (status == "1") {
                                AlertDialog.Builder(this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Berhasil")
                                        .setMessage("Data penjualan berhasil ditambahkan")
                                        .setPositiveButton("Iya") { dialog, which ->
                                            val i = Intent(this@TambahPenjualanActivity, KeranjangCanvassingActivity::class.java)
                                            startActivity(i)
                                            finish()
                                        }
                                        .show()
                            }else{
                                Snackbar.make(findViewById(R.id.btnNext), result.get("message").asString, Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Tutup") { }.show()
                            }
                        } catch (ex: Exception) {
                            Snackbar.make(findViewById(R.id.btnNext), "Terjadi kesalahan saaat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Tutup") { }.show()
                        }
                    }
            */
        }


    }

    private fun insertCart(cartData: Cart) {
        val task = Runnable {
            val result = mDb?.cartDao()?.checkProduct(cartData.toko_id,cartData.barang_id)

            mUiHandler.post({
                if (result == null || result?.size == 0) {
                    val task2 = Runnable {
                        mDb?.cartDao()?.insert(cartData)
                        mUiHandler.post({
                            val i = Intent(this@TambahPenjualanActivity, KeranjangCanvassingActivity::class.java)
                            startActivity(i)
                            finish()
                            /*
                            AlertDialog.Builder(this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Berhasil")
                                    .setMessage("Data penjualan berhasil ditambahkan")
                                    .setPositiveButton("Iya") { dialog, which ->
                                        val i = Intent(this@TambahPenjualanActivity, KeranjangCanvassingActivity::class.java)
                                        startActivity(i)
                                        finish()
                                    }
                                    .show()
                                    */
                        })
                    }
                    mDbWorkerThread.postTask(task2)
                }else{
                    val cart = result.get(0)
                    cart.qty = (cartData.qty.toInt() + cart.qty.toInt()).toString()

                    val task3 = Runnable {
                        mDb?.cartDao()?.update(cart)
                        mUiHandler.post({
                            val i = Intent(this@TambahPenjualanActivity, KeranjangCanvassingActivity::class.java)
                            startActivity(i)
                            finish()
                            /*
                            AlertDialog.Builder(this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Berhasil")
                                    .setMessage("Data penjualan berhasil ditambahkan")
                                    .setPositiveButton("Iya") { dialog, which ->
                                        val i = Intent(this@TambahPenjualanActivity, KeranjangCanvassingActivity::class.java)
                                        startActivity(i)
                                        finish()
                                    }
                                    .show()
                                    */
                        })
                    }
                    mDbWorkerThread.postTask(task3)
                }

            })
        }
        mDbWorkerThread.postTask(task)

    }

    override fun onDestroy() {
        AppDatabase.destroyInstance()
        mDbWorkerThread.quit()
        super.onDestroy()
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
