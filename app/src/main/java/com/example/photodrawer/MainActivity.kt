package com.example.photodrawer

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.photodrawer.Constants.REQUEST_CAMERA_PERMISSION
import com.example.photodrawer.fragments.CameraFragment
import com.example.photodrawer.fragments.DrawingFragment
import com.example.photodrawer.fragments.StorageFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener
{
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open_Drawer, R.string.Close_Drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setStartFragment(navigationView)
    }

    private fun setStartFragment(navigationView: NavigationView)
    {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, CameraFragment(this))
            .addToBackStack(null)
            .commit()
        navigationView.setCheckedItem(R.id.nav_camera)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId) {
            R.id.nav_camera -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, CameraFragment(this))
                    .addToBackStack(null).
                    commit()
            }
            R.id.nav_storage -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, StorageFragment(this))
                    .addToBackStack(null).
                    commit()
            }
            R.id.nav_draw -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, DrawingFragment(this))
                    .addToBackStack(null).
                    commit()
            }
            R.id.nav_about -> {
                Toast.makeText(this, getAboutText(), Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED
                || grantResults[1] == PackageManager.PERMISSION_DENIED)
            {
                Toast.makeText(this, R.string.camera_not_allowed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAboutText(): String
    {
        return "PhotoDrawer \n by Maxim Titov 2021"
    }

    override fun onBackPressed()
    {
        supportFragmentManager.popBackStackImmediate()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}