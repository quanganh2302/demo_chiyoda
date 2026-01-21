package com.example.myapplication.ui.navigation

import com.example.myapplication.ui.model.DrawerAction

interface DrawerNavigator {
    fun navigate(action: DrawerAction)
}
