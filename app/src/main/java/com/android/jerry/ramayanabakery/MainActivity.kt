package com.android.jerry.ramayanabakery

import android.content.Intent
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
import android.widget.ImageView
import android.widget.TextView
import com.android.jerry.ramayanabakery.utility.RequestServer
import com.android.jerry.ramayanabakery.utility.Session
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_toko -> {
                val i = Intent(this@MainActivity, KelolaTokoActivity::class.java)
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
}
