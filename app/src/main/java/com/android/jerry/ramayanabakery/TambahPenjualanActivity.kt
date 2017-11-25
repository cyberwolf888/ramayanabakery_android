package com.android.jerry.ramayanabakery

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_tambah_penjualan.*

class TambahPenjualanActivity : AppCompatActivity() {

    companion object {

        private val REQ_TOKO = 111
        private val REQ_PRODUK = 222
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_penjualan)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toko.setOnClickListener{ view ->
            val i = Intent(this@TambahPenjualanActivity, ListTokoActivity::class.java)
            startActivityForResult(i, REQ_TOKO)
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
            val id_toko = data.getStringExtra("id_toko")
            val nama_toko = data.getStringExtra("nama_toko")
            toko.setText(nama_toko)
        }
        if (requestCode == REQ_PRODUK && resultCode == Activity.RESULT_OK && data != null) {
            val id_produk = data.getStringExtra("id_produk")
            val nama_produk = data.getStringExtra("nama_produk")
            //etSatuan.setText(satuan)
        }
    }
}
