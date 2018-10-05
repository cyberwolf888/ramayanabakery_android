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
import com.google.gson.JsonObject
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_list_toko.*
import java.util.ArrayList
import java.util.HashMap

class ListTokoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_toko)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val url = RequestServer().getServer_url() + "list_toko.php"
        val jsonReq = JsonObject()
        jsonReq.addProperty("id", true)

        showProgress(true)
        Ion.with(this@ListTokoActivity)
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
                            dataList.put("id", objData.get("id_member").asString)
                            dataList.put("name", objData.get("nama_toko").asString)
                            xitemList.add(dataList)
                        }
                        val adapter = SimpleAdapter(
                                this@ListTokoActivity,
                                xitemList,
                                R.layout.list_toko,
                                arrayOf("name"),
                                intArrayOf(R.id.tvToko)
                        )
                        lvToko.adapter = adapter
                        lvToko.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                            val objSelected = mData.get(i).asJsonObject
                            setOutput(objSelected.get("id_member").asString, objSelected.get("nama_toko").asString)
                            finish()
                        }
                    } catch (ex: Exception) {
                        Snackbar.make(findViewById(R.id.lvToko), "Terjadi kesalahan saaat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Tutup") { }.show()
                    }
                    showProgress(false)
                }
    }

    private fun setOutput(id_toko:String, nama_toko:String){
        val output = Intent()
        output.putExtra("id_toko",id_toko)
        output.putExtra("nama_toko",nama_toko)
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
