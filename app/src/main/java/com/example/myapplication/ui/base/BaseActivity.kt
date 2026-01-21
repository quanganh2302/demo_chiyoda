package com.example.myapplication.ui.base

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.ui.navigation.DrawerFragment
import com.example.myapplication.ui.navigation.HeaderFragment
import com.example.myapplication.ui.model.DrawerAction
import com.example.myapplication.ui.utils.LanguageManager
import com.example.myapplication.ui.navigation.DrawerNavigator
import com.example.myapplication.ui.navigation.AppDrawerNavigator

abstract class BaseActivity : AppCompatActivity(),
    HeaderFragment.HeaderListener,
    DrawerFragment.DrawerListener {

    protected lateinit var drawerNavigator: DrawerNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        drawerNavigator = AppDrawerNavigator(this)
    }

    open fun hasDrawer(): Boolean = false

    override fun onMenuClicked() {
        // Activity có drawer sẽ override
    }

    override fun onDrawerItemSelected(action: DrawerAction) {
        drawerNavigator.navigate(action)
        onAfterDrawerNavigate()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase))
    }
    override fun onLanguageChanged(lang: String) {
        LanguageManager.setLanguage(this, lang)
        recreate()
    }

    protected fun attachHeader() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.headerFragment, HeaderFragment())
            .commit()
    }

    open fun onAfterDrawerNavigate() {

    }
}

