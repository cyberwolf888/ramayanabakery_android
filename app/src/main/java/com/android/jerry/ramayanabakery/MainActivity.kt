package com.android.jerry.ramayanabakery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.SimpleAdapter
import android.widget.TextView
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.android.jerry.ramayanabakery.utility.Session
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.ArrayList
import java.util.HashMap

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    internal var session: Session? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        session = Session(this@MainActivity)
        super.onCreate(savedInstanceState)
        if(!session!!.isLoggedIn){
            val i = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(i)
            finish()
        }
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val i = Intent(this@MainActivity, TambahPenjualanActivity::class.java)
            startActivity(i)
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val header = nav_view.getHeaderView(0)
        val name = header.findViewById<TextView>(R.id.username) as TextView
        val email = header.findViewById<TextView>(R.id.email) as TextView
        val avatar = header.findViewById<ImageView>(R.id.imageView) as ImageView
        if (session!!.isLoggedIn()) {
            name.text = session!!.getFullname()
            email.text = session!!.getEmail()
            if (!session!!.getPhoto().equals("")) {
                Ion.with(this)
                        .load(RequestServer().getImg_url()+ session!!.getPhoto())
                        .withBitmap()
                        .placeholder(R.drawable.guest)
                        .error(R.drawable.guest)
                        .intoImageView(avatar)
            }
            //Log.d("img", ">" + RequestServer().getServer_url() + "../assets/img/profile/" + session!!.getPhoto())
        } else {
            name.text = "Guest"
            email.visibility = View.GONE
        }
    }

    public override fun onResume() {
        super.onResume()
        if(!session!!.isLoggedIn){
            val i = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(i)
            finish()
        }

        getData()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Keluar")
                    .setMessage("Apakah anda yakin akan keluar dari aplikasi?")
                    .setPositiveButton("Iya") { dialog, which -> finish() }
                    .setNegativeButton("Tidak", null)
                    .show()
        }
    }

    fun getData(){
        val url = RequestServer().getServer_url() + "list_transaksi.php"
        val jsonReq = JsonObject()
        jsonReq.addProperty("id_pegawai", session!!.getUserId())

        showProgress(true)

        Ion.with(this@MainActivity)
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
                            dataList.put("no_history", objData.get("no_history").asString)
                            dataList.put("id_pegawai", objData.get("id_pegawai").asString)
                            dataList.put("tgl_history", objData.get("tgl_history").asString)
                            dataList.put("type", objData.get("type").asString)
                            dataList.put("qty", objData.get("qty").asString)
                            dataList.put("total_price", objData.get("total_price").asString)
                            dataList.put("nama_product", objData.get("nama_product").asString)
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
                                this@MainActivity,
                                xitemList,
                                R.layout.list_transaksi,
                                arrayOf("nama_toko","nama_product","tgl_history","status"),
                                intArrayOf(R.id.tvMember,R.id.tvProduct,R.id.tvTgl,R.id.tvType)
                        )
                        lvTransaksi.adapter = adapter
                        lvTransaksi.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                            val objSelected = mData.get(i).asJsonObject
                            //setOutput(objSelected.get("id").asString, objSelected.get("name").asString)
                            //finish()

                            if(objSelected.get("status").asString.equals("1")){
//                                Snackbar.make(findViewById(R.id.lvTransaksi), "Lunasi transaksi "+objSelected.get("no_history").asString+" ?", Snackbar.LENGTH_LONG)
//                                        .setAction("Lunasi") { lunas(objSelected.get("no_history").asString) }.show()
                                val i = Intent(this@MainActivity, LunasActivity::class.java)
                                i.putExtra("no_history", objSelected.get("no_history").asString)
                                startActivity(i)

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
        val url = RequestServer().getServer_url() + "bayar_transaksi.php"
        val jsonReq = JsonObject()
        jsonReq.addProperty("no_history", no_history)
        Ion.with(this@MainActivity)
                .load(url)
                //.setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback { e, result ->
                    getData()
                }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_bo -> {
                val i = Intent(this@MainActivity, BookingActivity::class.java)
                startActivity(i)
            }
            R.id.nav_toko -> {
                val i = Intent(this@MainActivity, KelolaTokoActivity::class.java)
                startActivity(i)
            }
            R.id.nav_barang -> {
                val i = Intent(this@MainActivity, BarangActivity::class.java)
                startActivity(i)
            }
            R.id.nav_myaccount -> {
                val i = Intent(this@MainActivity, ProfileActivity::class.java)
                startActivity(i)
            }
            R.id.nav_logout -> {
                AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Logout")
                        .setMessage("Apakah anda yakin akan logout dari aplikasi?")
                        .setPositiveButton("Iya") { dialog, which -> session!!.logoutUser() }
                        .setNegativeButton("Tidak", null)
                        .show()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
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
