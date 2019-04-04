package com.android.jerry.ramayanabakery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.SimpleAdapter
import com.android.jerry.ramayanabakery.database.AppDatabase
import com.android.jerry.ramayanabakery.database.entities.Cart
import com.android.jerry.ramayanabakery.utility.DbWorkerThread
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.android.jerry.ramayanabakery.utility.Session
import com.google.gson.JsonObject

import kotlinx.android.synthetic.main.activity_keranjang_canvassing.*
import kotlinx.android.synthetic.main.content_keranjang_canvassing.*
import java.lang.Exception
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.HashMap
import com.google.gson.Gson
import com.koushikdutta.ion.Ion


class KeranjangCanvassingActivity : AppCompatActivity() {

    internal var session: Session? = null
    private var cartData: List<Cart>? = null
    private var mDb: AppDatabase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        session = Session(this@KeranjangCanvassingActivity)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keranjang_canvassing)
        setSupportActionBar(toolbar)

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()
        mDb = AppDatabase.getInstance(this@KeranjangCanvassingActivity)

        fab.setOnClickListener { view ->
            val i = Intent(this@KeranjangCanvassingActivity, TambahPenjualanActivity::class.java)
            startActivity(i)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun fetchCartDataFromDb() {
        showProgress(true)
        val task = Runnable {
            val request =
                    mDb?.cartDao()?.getOrderByToko()
            mUiHandler.post({
                if (request == null || request?.size == 0) {
                    showProgress(false)
                    Snackbar.make(findViewById(R.id.lvCart), "Keranjang belanja kosong", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Tutup") { }.show()
                } else {
                    cartData = request
                    buildList(request)
                }
            })
        }

        try{
            mDbWorkerThread.postTask(task)
        }catch (e:Exception){
            finish()
        }

    }

    private fun buildList(carts:List<Cart>) {
        Log.d("Room DB",">" + carts)
        val xitemList = ArrayList<HashMap<String, String>>()
        val formatter = DecimalFormat("#,###,###")
        for(cart:Cart in carts){
            //Log.d("Cart Data",">" + cart.nama_produk)
            val dataList = HashMap<String, String>()
            dataList.put("nama_toko", cart.nama_toko)
            dataList.put("nama_produk", cart.nama_produk)
            dataList.put("qty", "Qty : "+cart.qty)
            dataList.put("harga", "Harga : "+cart.harga)
            dataList.put("total", "Total : "+formatter.format(cart.harga.toInt()*cart.qty.toInt()).toString())
            xitemList.add(dataList)
        }

        //Log.d("xitemList",">" + xitemList)

        val adapter = SimpleAdapter(
                this@KeranjangCanvassingActivity,
                xitemList,
                R.layout.list_cart,
                arrayOf("nama_toko","nama_produk","harga","qty","total"),
                intArrayOf(R.id.tvMember,R.id.tvProduct,R.id.tvHarga,R.id.tvQty,R.id.tvTotal)
        )
        lvCart.adapter = adapter
        lvCart.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val objSelected = carts.get(i)
            Snackbar.make(findViewById(R.id.lvCart), "Hapus data "+objSelected.nama_produk+" dari keranjang?", Snackbar.LENGTH_LONG)
                                       .setAction("Hapus") { deleteCart(objSelected) }.show()
        }
        showProgress(false)
    }

    private fun deleteCart(cart:Cart){
        val task = Runnable {
            mDb?.cartDao()?.deleteCart(cart)
            mUiHandler.post({
              fetchCartDataFromDb()
            })
        }

        mDbWorkerThread.postTask(task)
    }

    private fun clearCart(){
        val task = Runnable {
            mDb?.cartDao()?.deleteAll()
            mUiHandler.post({
                finish()
            })
        }

        mDbWorkerThread.postTask(task)
    }

    private fun checkoutCart(){
        showProgress(true)
        Log.d("Cart Data", ">" + cartData)
        val url = RequestServer().getServer_url() + "simpan_transaksi.php"
        Log.d("Url", ">" + url)

        val jsonCart = Gson().toJson(cartData)
        val jsonReq = JsonObject()
        jsonReq.addProperty("id_pegawai", session!!.getUserId())
        jsonReq.addProperty("carts", jsonCart)

        Log.d("Cek Req", ">" + jsonReq)

        Ion.with(this@KeranjangCanvassingActivity)
                .load(url)
                //.setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback { e, result ->
                    Log.d("Response", ">" + result)
                    showProgress(false)
                    try {
                        val status = result.get("status").toString()
                        if (status == "1") {
                            AlertDialog.Builder(this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Berhasil")
                                    .setMessage("Data penjualan berhasil ditambahkan")
                                    .setPositiveButton("Iya") { dialog, which ->
                                        clearCart()

                                    }
                                    .show()
                        }else{
                            Snackbar.make(findViewById(R.id.lvCart), result.get("message").asString, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Tutup") { }.show()
                        }
                    } catch (ex: Exception) {
                        Snackbar.make(findViewById(R.id.lvCart), "Terjadi kesalahan saaat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Tutup") { }.show()
                    }
                }
    }

    override fun onDestroy() {
        AppDatabase.destroyInstance()
        mDbWorkerThread.quit()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        fetchCartDataFromDb()
    }

    override fun onCreateOptionsMenu(menu: Menu):Boolean {
        getMenuInflater().inflate(R.menu.checkout, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_checkout -> {
                if (cartData == null || cartData?.size == 0) {

                }else{
                    checkoutCart()
                }

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

            lvCart.visibility = if (show) View.GONE else View.VISIBLE
            lvCart.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            lvCart.visibility = if (show) View.GONE else View.VISIBLE
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
            lvCart.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

}
