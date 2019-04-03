package com.android.jerry.ramayanabakery

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion

import kotlinx.android.synthetic.main.activity_detail_canvasing.*

class DetailCanvasingActivity : AppCompatActivity() {

    var no_history:String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_canvasing)
        setSupportActionBar(toolbar)

        no_history = intent.getStringExtra("no_history")

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Lunasi transaksi ini ?", Snackbar.LENGTH_LONG)
                    .setAction("Lunasi", { lunasiTransaksi() }).show()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun lunasiTransaksi(){

    }

    private fun getDetailTransaksi(){
        val url = RequestServer().getServer_url() + "detail_transaksi.php"
        val jsonReq = JsonObject()
        jsonReq.addProperty("no_history", no_history)
        Ion.with(this@DetailCanvasingActivity)
                .load(url)
                //.setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback { e, result ->
                    Log.d("Response", ">" + result)
                }

    }

    override fun onResume() {
        super.onResume()
        getDetailTransaksi()
    }

}
