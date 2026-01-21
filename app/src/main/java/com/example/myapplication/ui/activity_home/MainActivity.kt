package com.example.myapplication.ui.activity_home

import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.ui.base.BaseActivity
import com.example.myapplication.ui.model.DrawerAction

class MainActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun hasDrawer(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)

        attachHeader()

        if (savedInstanceState == null) {
            drawerNavigator.navigate(DrawerAction.HOME)
        }
    }

    override fun onMenuClicked() {
        drawerLayout.open()
    }
    override fun onAfterDrawerNavigate() {
        drawerLayout.closeDrawers()
    }
}
