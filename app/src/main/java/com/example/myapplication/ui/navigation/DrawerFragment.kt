package com.example.myapplication.ui.navigation

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ui.adapter.DrawerMenuAdapter
import com.example.myapplication.ui.model.DrawerAction
import com.example.myapplication.ui.model.DrawerMenuItem

class DrawerFragment : Fragment(R.layout.fragment_drawer) {

    interface DrawerListener {
        fun onDrawerItemSelected(action: DrawerAction)
    }

    private var listener: DrawerListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? DrawerListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerDrawer)

        val items = listOf(
            DrawerMenuItem(
                DrawerAction.HOME,
                R.drawable.ic_home,
                R.string.drawer_home
            ),
            DrawerMenuItem(
                DrawerAction.SETTINGS,
                R.drawable.ic_settings,
                R.string.drawer_settings
            ),
            DrawerMenuItem(
                DrawerAction.EXIT,
                R.drawable.ic_exit,
                R.string.drawer_exit
            )
        )


        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = DrawerMenuAdapter(items) {
            listener?.onDrawerItemSelected(it.action)
        }
    }
}