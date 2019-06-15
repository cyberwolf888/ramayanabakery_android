package com.android.jerry.ramayanabakery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.SimpleAdapter
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_detail_booking.*
import kotlinx.android.synthetic.main.content_detail_booking.*
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.HashMap

class DetailBookingActivity : AppCompatActivity() {

    var no_history:String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_booking)

        no_history = intent.getStringExtra("no_history")

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Lunasi booking ini ?", Snackbar.LENGTH_LONG)
                    .setAction("Lunasi", { lunasiTransaksi() }).show()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun lunasiTransaksi(){
        showProgress(true)
        val url = RequestServer().getServer_url() + "bayar_bo.php"
        val jsonReq = JsonObject()
        jsonReq.addProperty("no_history", no_history)
        Ion.with(this@DetailBookingActivity)
                .load(url)
                //.setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback { e, result ->
                    //getData()
                    getDetailTransaksi()
                }
    }

    private fun getDetailTransaksi(){
        showProgress(true)
        val url = RequestServer().getServer_url() + "detail_booking.php"
        val jsonReq = JsonObject()
        jsonReq.addProperty("no_history", no_history)
        Ion.with(this@DetailBookingActivity)
                .load(url)
                //.setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback { e, result ->
                    //Log.d("Response", ">" + result)
                    try {
                        val mData = result.getAsJsonObject("data")
                        val data_barang = mData.get("barang").asJsonArray
                        val xitemList = ArrayList<HashMap<String, String>>()
                        val formatter = DecimalFormat("#,###,###")
                        tvToko.setText(mData.get("nama_toko").asString)
                        tvNoTransaksi.setText(mData.get("no_history").asString)
                        tvTgl.setText(mData.get("label_tanggal").asString)
                        tvTotal.setText("Rp. "+formatter.format(mData.get("total").asNumber).toString())
                        if(mData.get("status").asString.equals("2")){
                            tvStatus.setText("Lunas")
                            fab.setVisibility(View.GONE)
                        }else{
                            tvStatus.setText("Proses")
                        }

                        for (i in 0 until data_barang.size()) {
                            val objData = data_barang.get(i).asJsonObject
                            val dataList = HashMap<String, String>()
                            dataList.put("nama_product", objData.get("nama_product").asString)
                            dataList.put("qty", "Qty: "+objData.get("qty").asString)
                            dataList.put("harga", "Harga: Rp. "+formatter.format(objData.get("harga").asNumber).toString())
                            dataList.put("total_price", "Total: Rp. "+formatter.format(objData.get("total_price").asNumber).toString())
                            xitemList.add(dataList)
                        }

                        val adapter = SimpleAdapter(
                                this@DetailBookingActivity,
                                xitemList,
                                R.layout.list_detail_transaksi,
                                arrayOf("nama_product","harga","qty","total_price"),
                                intArrayOf(R.id.tvNamaProduk,R.id.tvHarga,R.id.tvQty,R.id.tvTotal)
                        )
                        lvDetailTransaksi.adapter = adapter
                        showProgress(false)
                    }catch (ex: Exception) {
                        showProgress(false)
                        Snackbar.make(findViewById(R.id.main_layout), "Tidak ada data transaksi.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Tutup") { }.show()
                    }

                }

    }

    override fun onResume() {
        super.onResume()
        getDetailTransaksi()
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            main_layout.visibility = if (show) View.GONE else View.VISIBLE
            main_layout.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            main_layout.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            req_progress.visibility = if (show) View.VISIBLE else View.GONE
            req_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            req_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            req_progress.visibility = if (show) View.VISIBLE else View.GONE
            main_layout.visibility = if (show) View.GONE else View.VISIBLE
        }
    }
}
