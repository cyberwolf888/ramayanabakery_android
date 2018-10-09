package com.android.jerry.ramayanabakery

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.MenuItem
import android.view.View
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.android.jerry.ramayanabakery.utility.Session
import com.google.gson.JsonObject
import com.koushikdutta.async.future.Future
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File

class ProfileActivity : AppCompatActivity() {
    private val WRITE_EXTERNAL_RESULT = 105
    private val SELECT_PHOTO = 12345
    internal var session: Session? = null
    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        session = Session(this@ProfileActivity)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        getData()

        if (!mayRequestPermission()) {
            return
        }

        imageView.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, SELECT_PHOTO)
        }

        btnSimpan.setOnClickListener {
            simpanData()
        }

    }

    override fun onResume() {
        super.onResume()

    }

    fun getData(){
        showProgress(true)
        val url = RequestServer().getServer_url() + "profile.php"
        val jsonReq = JsonObject()
        jsonReq.addProperty("id_pegawai", session!!.getUserId())
        Log.d("req", ">" + jsonReq)

        Ion.with(this@ProfileActivity)
                .load(url)
                //.setLogging("ION_VERBOSE_LOGGING", Log.VERBOSE)
                .setJsonObjectBody(jsonReq)
                .asJsonObject()
                .setCallback { e, result ->
                    Log.d("Response", ">" + result)

                    try {
                        val data = result.getAsJsonObject("data")

                        var photo = ""
                        if (!data.get("foto").isJsonNull) {
                            photo = data.get("foto").asString
                        }
                        Log.d("photo", ">" + photo)
                        if (!photo.equals("")) {
                            Ion.with(this)
                                    .load(RequestServer().getImg_url()+ photo)
                                    .withBitmap()
                                    .placeholder(R.drawable.guest)
                                    .error(R.drawable.guest)
                                    .intoImageView(imageView)
                        }

                        tvId.text = data.get("id_user").asString
                        tvNama.text = data.get("nama_pegawai").asString
                        tvTempatLahir.text = data.get("tempat_lahir").asString
                        tvTglLahir.text = data.get("tgl_lahir").asString
                        tvAlamat.text = data.get("alamat").asString
                        tvJenisKelamin.text = data.get("jenis_kelamin").asString
                        tvPendidikan.text = data.get("pendidikan").asString
                        tvJabatan.text = data.get("jabatan").asString

                    } catch (ex: Exception) {
                        Snackbar.make(findViewById(R.id.profile_form), "Terjadi kesalahan saaat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Tutup") { }.show()
                    }

                    showProgress(false)
                }
    }

    fun simpanData(){
        etPassword.error = null

        val passwordStr = etPassword.text.toString()

        var cancel = false
        var focusView: View? = null

        if (TextUtils.isEmpty(passwordStr)) {
            etPassword.error = getString(R.string.error_invalid_password)
            focusView = etPassword
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            showProgress(true)

            val url = RequestServer().getServer_url() + "simpan_profile.php"

            if(!imagePath.isNullOrEmpty()){
                Ion.with(this@ProfileActivity).load(url)
                        .setMultipartFile("foto", "image/jpeg", File(imagePath))
                        .setMultipartParameter("password", passwordStr)
                        .setMultipartParameter("id_pegawai", session!!.getUserId())
                        .asJsonObject()
                        .setCallback { e, result ->
                            showProgress(false)
                            Log.d("Response", ">" + result)
                            val status = result.get("status").toString()
                            if (status == "1") {
                                AlertDialog.Builder(this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Berhasil")
                                        .setMessage("Data profile berhasil disimpan.")
                                        .setPositiveButton("Iya") { dialog, which -> session!!.logoutUser() }
                                        .show()
                            }else{
                                Snackbar.make(findViewById(R.id.profile_form), "Terjadi kesalahan saaat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Tutup") { }.show()
                            }
                        }
            }else{
                Ion.with(this@ProfileActivity).load(url)
                        .setMultipartParameter("password", passwordStr)
                        .setMultipartParameter("id_pegawai", session!!.getUserId())
                        .asJsonObject()
                        .setCallback { e, result ->
                            showProgress(false)
                            Log.d("Response", ">" + result)
                            val status = result.get("status").toString()
                            if (status == "1") {
                                AlertDialog.Builder(this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Berhasil")
                                        .setMessage("Data profile berhasil disimpan.")
                                        .setPositiveButton("Iya") { dialog, which -> finish() }
                                        .show()
                            }else{
                                Snackbar.make(findViewById(R.id.profile_form), "Terjadi kesalahan saaat menyambung ke server.", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Tutup") { }.show()
                            }

                        }
            }
        }
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

    private fun mayRequestPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true
        }

        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
            Snackbar.make(profile_form, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) { requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), WRITE_EXTERNAL_RESULT) }
        } else {
            requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), WRITE_EXTERNAL_RESULT)
        }

        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == WRITE_EXTERNAL_RESULT) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission diterima
            } else {
                //permission ditolak
                mayRequestPermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            val pickedImage = data.data
            val filePath = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(pickedImage!!, filePath, null, null, null)
            cursor!!.moveToFirst()
            imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]))
            //Cek file size
            val file = File(imagePath)
            val file_size = Integer.parseInt((file.length() / 1024).toString())
            Log.d("File Size", ">$file_size")
            if (file_size > 3 * 1024) {
                imagePath = ""
                Snackbar.make(profile_form, "Ukuran gambar terlalu besar. Ukuran file maksimal 3 MB.", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Tutup") { }.show()
            } else {
                /*BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);*/
                imageView.setImageBitmap(decodeSampledBitmapFromResource(imagePath!!, 100, 100))
            }

            cursor.close()
        }
    }

    fun calculateInSampleSize(
            options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun decodeSampledBitmapFromResource(res: String, reqWidth: Int, reqHeight: Int): Bitmap {

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(res, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(res, options)
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

            profile_form.visibility = if (show) View.GONE else View.VISIBLE
            profile_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            profile_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            loading_progress.visibility = if (show) View.VISIBLE else View.GONE
            loading_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            loading_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loading_progress.visibility = if (show) View.VISIBLE else View.GONE
            profile_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }
}
