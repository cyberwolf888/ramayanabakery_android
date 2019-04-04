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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.SimpleAdapter
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.android.jerry.ramayanabakery.utility.Session
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion

import kotlinx.android.synthetic.main.activity_booking.*
import kotlinx.android.synthetic.main.content_booking.*
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.HashMap

class BookingActivity : AppCompatActivity() {
    internal var session: Session? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        session = Session(this@BookingActivity)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val i = Intent(this@BookingActivity, TambahBookingActivity::class.java)
            startActivity(i)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    public override fun onResume() {
        super.onResume()

        getData()
    }

    fun getData(){
        val url = RequestServer().getServer_url() + "list_bo.php"
        val jsonReq = JsonObject()
        jsonReq.addProperty("id_pegawai", session!!.getUserId())

        showProgress(true)

        Ion.with(this@BookingActivity)
                .load(url)
                //.setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback { e, result ->
                    Log.d("Response", ">" + result)
                    try {
                        val mData = result.getAsJsonArray("data")
                        val xitemList = ArrayList<HashMap<String, String>>()
                        val formatter = DecimalFormat("#,###,###")
                        for (i in 0 until mData.size()) {
                            val objData = mData.get(i).asJsonObject
                            val dataList = HashMap<String, String>()
                            dataList.put("no_history", objData.get("no_history").asString)
                            dataList.put("id_pegawai", objData.get("id_pegawai").asString)
                            dataList.put("tgl_history", objData.get("tgl_history").asString)
                            dataList.put("type", objData.get("type").asString)
                            dataList.put("qty", objData.get("qty").asString)
                            dataList.put("total_price", "Rp. " + formatter.format(objData.get("total_price").asNumber).toString())
                            dataList.put("nama_product", objData.get("nama_product").asString + " (" + objData.get("qty").asString + " buah)")
                            dataList.put("nama_member", objData.get("nama_member").asString)
                            dataList.put("nama_toko", objData.get("nama_toko").asString)
                            dataList.put("status", objData.get("status").asString)

                            if(objData.get("status").asString.equals("2")){
                                dataList.put("status", "Lunas")
                            }else{
                                dataList.put("status", "Proses")
                            }
                            dataList.put("title", "["+objData.get("no_history").asString+"] "+objData.get("nama_toko").asString)
                            xitemList.add(dataList)
                        }
                        val adapter = SimpleAdapter(
                                this@BookingActivity,
                                xitemList,
                                R.layout.list_transaksi,
                                arrayOf("nama_toko","nama_product","tgl_history","total_price","status"),
                                intArrayOf(R.id.tvMember,R.id.tvProduct,R.id.tvTgl,R.id.tvTotal,R.id.tvType)
                        )
                        lvTransaksi.adapter = adapter
                        lvTransaksi.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                            val objSelected = mData.get(i).asJsonObject
                            //setOutput(objSelected.get("id").asString, objSelected.get("name").asString)
                            //finish()

                            if(objSelected.get("status").asString.equals("1")){
                                Snackbar.make(findViewById(R.id.lvTransaksi), "Selesaikan transaksi "+objSelected.get("no_history").asString+" ?", Snackbar.LENGTH_LONG)
                                        .setAction("Selesai") { lunas(objSelected.get("no_history").asString) }.show()


                            }
                        }
                    }catch (ex: Exception) {
                        Snackbar.make(findViewById(R.id.lvTransaksi), "Tidak ada data transaksi.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Tutup") { }.show()
                    }


                    showProgress(false)
                }
    }

    fun lunas(no_history: String){
        val url = RequestServer().getServer_url() + "bayar_bo.php"
        val jsonReq = JsonObject()
        jsonReq.addProperty("no_history", no_history)
        Ion.with(this@BookingActivity)
                .load(url)
                //.setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback { e, result ->
                    getData()
                }
    }

    override fun onCreateOptionsMenu(menu: Menu):Boolean {
        getMenuInflater().inflate(R.menu.keranjang_belanja, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_cart -> {
                val i = Intent(this, KeranjangCanvassingActivity::class.java)
                startActivity(i)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
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

            lvTransaksi.visibility = if (show) View.GONE else View.VISIBLE
            lvTransaksi.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            lvTransaksi.visibility = if (show) View.GONE else View.VISIBLE
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
            lvTransaksi.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

}
