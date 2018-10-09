package com.android.jerry.ramayanabakery

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.android.jerry.ramayanabakery.utility.Session
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_tambah_penjualan.*

class TambahPenjualanActivity : AppCompatActivity() {

    private var id_toko: String? = null
    private var id_produk: String? = null
    internal var session: Session? = null

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
            qty.error = getString(R.string.error_invalid_password)
            focusView = qty
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
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
                                        .setPositiveButton("Iya") { dialog, which -> finish() }
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
        }


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
            val nama_toko = data.getStringExtra("nama_toko")
            toko.setText(nama_toko)
        }
        if (requestCode == REQ_PRODUK && resultCode == Activity.RESULT_OK && data != null) {
            id_produk = data.getStringExtra("id_produk")
            val nama_produk = data.getStringExtra("nama_produk")
            produk.setText(nama_produk)
        }
    }
}
