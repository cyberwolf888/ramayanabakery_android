package com.android.jerry.ramayanabakery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.SimpleAdapter
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion

import kotlinx.android.synthetic.main.activity_kelola_toko.*
import kotlinx.android.synthetic.main.content_kelola_toko.*
import java.util.ArrayList
import java.util.HashMap

class KelolaTokoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kelola_toko)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val i = Intent(this@KelolaTokoActivity, TambahTokoActivity::class.java)
            startActivity(i)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    fun getData(){
        val url = RequestServer().getServer_url() + "list_toko.php"
        val jsonReq = JsonObject()
        jsonReq.addProperty("id", true)

        showProgress(true)
        Ion.with(this@KelolaTokoActivity)
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
                            dataList.put("id_member", objData.get("id_member").asString)
                            dataList.put("nama_member", objData.get("nama_member").asString)
                            dataList.put("alamat", objData.get("alamat").asString)
                            dataList.put("no_telp", objData.get("no_telp").asString)
                            dataList.put("nama_toko", objData.get("nama_member").asString)
                            xitemList.add(dataList)
                        }
                        val adapter = SimpleAdapter(
                                this@KelolaTokoActivity,
                                xitemList,
                                R.layout.list_kelola_toko,
                                arrayOf("nama_toko","no_telp","alamat"),
                                intArrayOf(R.id.tvToko,R.id.tvNoTelp,R.id.tvAlamat)
                        )
                        lvToko.adapter = adapter
                        lvToko.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                            val objSelected = mData.get(i).asJsonObject
                            //setOutput(objSelected.get("id").asString, objSelected.get("name").asString)
                            //finish()
                            Snackbar.make(findViewById(R.id.lvToko), objSelected.get("nama_toko").asString, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Tutup") { }.show()
                        }
                    } catch (ex: Exception) {
                        Snackbar.make(findViewById(R.id.lvToko), "Terjadi kesalahan saaat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Tutup") { }.show()
                    }
                    showProgress(false)
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

            lvToko.visibility = if (show) View.GONE else View.VISIBLE
            lvToko.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            lvToko.visibility = if (show) View.GONE else View.VISIBLE
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
            lvToko.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

}
