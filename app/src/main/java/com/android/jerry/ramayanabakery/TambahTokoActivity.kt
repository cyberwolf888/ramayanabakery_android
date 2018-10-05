package com.android.jerry.ramayanabakery

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.google.gson.JsonObject
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_tambah_toko.*

class TambahTokoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_toko)
        btnTambah.setOnClickListener { tambahToko() }
    }

    fun tambahToko(){
        etNamaMember.error = null
        etAlamat.error = null
        etNoTelp.error = null
        etNamaToko.error = null

        val etNamaMemberStr = etNamaMember.text.toString()
        val etAlamatStr = etAlamat.text.toString()
        val etNoTelpStr = etNoTelp.text.toString()
        val etNamaTokoStr = etNamaToko.text.toString()

        var cancel = false
        var focusView: View? = null

        if (TextUtils.isEmpty(etNamaMemberStr)) {
            etNamaMember.error = getString(R.string.error_field_required)
            focusView = etNamaMember
            cancel = true
        }

        if (TextUtils.isEmpty(etAlamatStr)) {
            etAlamat.error = getString(R.string.error_field_required)
            focusView = etAlamat
            cancel = true
        }

        if (TextUtils.isEmpty(etNoTelpStr)) {
            etNoTelp.error = getString(R.string.error_field_required)
            focusView = etNoTelp
            cancel = true
        }

        if (TextUtils.isEmpty(etNamaTokoStr)) {
            etNamaToko.error = getString(R.string.error_field_required)
            focusView = etNamaToko
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            showProgress(true)
            val url = RequestServer().getServer_url() + "add_toko.php"
            Log.d("Login Url", ">" + url)

            val jsonReq = JsonObject()
            jsonReq.addProperty("nama_member", etNamaMemberStr)
            jsonReq.addProperty("alamat", etAlamatStr)
            jsonReq.addProperty("no_telp", etNoTelpStr)
            jsonReq.addProperty("nama_toko", etNamaTokoStr)
            Log.d("Cek Req", ">" + jsonReq)

            Ion.with(this@TambahTokoActivity)
                    .load(url)
                    //.setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
                    .setJsonObjectBody(jsonReq)
                    .asJsonObject()
                    .setCallback { e, result ->
                        Log.d("Response", ">" + result)

                        try {
                            val status = result.get("status").toString()
                            if (status == "1") {
                                showProgress(false)
                                AlertDialog.Builder(this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Berhasil")
                                        .setMessage("Data Toko berhasil ditambahkan.")
                                        .setPositiveButton("Iya") { dialog, which -> finish() }
                                        .show()
                            }else{
                                showProgress(false)
                                Snackbar.make(findViewById(R.id.login_form), result.get("message").toString(), Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Tutup") { }.show()
                            }
                        }catch (ex: Exception) {
                            showProgress(false)
                            Snackbar.make(findViewById(R.id.login_form), "Terjadi kesalahan saaat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Tutup") { }.show()
                        }
                        showProgress(false)
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

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }
}
