package com.android.jerry.ramayanabakery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.widget.SimpleAdapter
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.android.jerry.ramayanabakery.utility.Session
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_barang.*
import java.util.ArrayList
import java.util.HashMap

class BarangActivity : AppCompatActivity() {
    internal var session: Session? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        session = Session(this@BarangActivity)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barang)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    fun getData(){
        showProgress(true)
        val url = RequestServer().getServer_url() + "barang_sales.php"
        val jsonReq = JsonObject()
        jsonReq.addProperty("pegawai_id", session!!.getUserId())
        Log.d("req", ">" + jsonReq)

        Ion.with(this@BarangActivity)
                .load(url)
                //.setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback { e, result ->
                    Log.d("Response", ">" + result)
                    try {
                        val mData = result.getAsJsonArray("data")
                        val xitemList = ArrayList<HashMap<String, String>>()
                        for (i in 0 until mData.size()) {
                            val objData = mData.get(i).asJsonObject
                            val dataList = HashMap<String, String>()
                            dataList.put("id", objData.get("id_product").asString)
                            dataList.put("name", objData.get("nama_product").asString)
                            dataList.put("qty", "Stock : "+objData.get("qty").asString)
                            xitemList.add(dataList)
                        }
                        val adapter = SimpleAdapter(
                                this@BarangActivity,
                                xitemList,
                                R.layout.list_barang,
                                arrayOf("name","qty"),
                                intArrayOf(R.id.tvProduct,R.id.tvStock)
                        )
                        lvBarang.adapter = adapter
                        showProgress(false)
                    } catch (ex: Exception) {
                        showProgress(false)
                        Snackbar.make(findViewById(R.id.lvBarang), "Terjadi kesalahan saaat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Tutup") { }.show()
                    }
                }
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

            lvBarang.visibility = if (show) View.GONE else View.VISIBLE
            lvBarang.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            lvBarang.visibility = if (show) View.GONE else View.VISIBLE
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
            lvBarang.visibility = if (show) View.GONE else View.VISIBLE
        }
    }
}
