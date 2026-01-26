package com.example.myapplication.ui.base

import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.myapplication.R
import com.example.myapplication.ui.fragments.HomeFragment

class MainActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun hasDrawer(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        attachHeader()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    HomeFragment()
                )
                .commit()
        }
    }

    override fun onMenuClicked() {
        drawerLayout.open()
    }

    override fun onAfterDrawerNavigate() {
        drawerLayout.closeDrawers()
    }
}