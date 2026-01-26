package com.example.myapplication.ui.navigation

import androidx.fragment.app.FragmentActivity
import com.example.myapplication.R
import com.example.myapplication.ui.fragments.HomeFragment
import com.example.myapplication.ui.fragments.SettingsFragment
import com.example.myapplication.ui.model.DrawerAction

class AppDrawerNavigator(
    private val activity: FragmentActivity
) : DrawerNavigator {

    override fun navigate(action: DrawerAction) {
        when (action) {
            DrawerAction.HOME ->
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()

            DrawerAction.SETTINGS ->
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SettingsFragment())
                    .commit()

            DrawerAction.EXIT ->
                activity.finish()
        }
    }
}

