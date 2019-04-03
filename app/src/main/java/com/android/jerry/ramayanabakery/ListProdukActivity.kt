package com.android.jerry.ramayanabakery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.SimpleAdapter
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.android.jerry.ramayanabakery.utility.Session
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_list_produk.*
import java.util.ArrayList
import java.util.HashMap

class ListProdukActivity : AppCompatActivity() {
    internal var session: Session? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        session = Session(this@ListProdukActivity)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_produk)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val url = RequestServer().getServer_url() + "list_barang.php"
        val jsonReq = JsonObject()
        jsonReq.addProperty("id", session!!.getUserId())
        Log.d("req", ">" + jsonReq)
        showProgress(true)
        Ion.with(this@ListProdukActivity)
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
                            xitemList.add(dataList)
                        }
                        val adapter = SimpleAdapter(
                                this@ListProdukActivity,
                                xitemList,
                                R.layout.list_product,
                                arrayOf("name"),
                                intArrayOf(R.id.tvProduct)
                        )
                        lvProduct.adapter = adapter
                        lvProduct.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                            val objSelected = mData.get(i).asJsonObject
                            setOutput(objSelected.get("id_product").asString, objSelected.get("nama_product").asString, objSelected.get("harga").asString)
                            finish()
                        }
                    } catch (ex: Exception) {
                        Snackbar.make(findViewById(R.id.lvProduct), "Terjadi kesalahan saaat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Tutup") { }.show()
                    }
                    showProgress(false)
                }
    }

    private fun setOutput(id_toko:String, nama_toko:String, harga:String){
        val output = Intent()
        output.putExtra("id_produk",id_toko)
        output.putExtra("nama_produk",nama_toko)
        output.putExtra("harga",harga)
        setResult(Activity.RESULT_OK, output)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
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

            lvProduct.visibility = if (show) View.GONE else View.VISIBLE
            lvProduct.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            lvProduct.visibility = if (show) View.GONE else View.VISIBLE
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
            lvProduct.visibility = if (show) View.GONE else View.VISIBLE
        }
    }
}
