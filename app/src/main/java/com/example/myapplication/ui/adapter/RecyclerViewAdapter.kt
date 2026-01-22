package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.models.MasterLabelData

class RecyclerViewAdapter (
    private val items: MutableList<MasterLabelData>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    private var lastClickTime = 0L
    private val CLICK_DELAY_MS = 500L

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProductCode: TextView = view.findViewById(R.id.tvProductCode)
        val tvOrder: TextView = view.findViewById(R.id.tvOrder)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvQty: TextView = view.findViewById(R.id.tvQty)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_info, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvProductCode.text = item.productCode
        holder.tvOrder.text = "${item.wono}"
        holder.tvDate.text = "${item.date}"
        holder.tvQty.text = "${item.qty}"

        holder.btnDelete.setOnClickListener {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastClickTime < CLICK_DELAY_MS) {
                return@setOnClickListener
            }

            lastClickTime = currentTime

            val adapterPosition = holder.adapterPosition

            if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < items.size) {
                onDelete(adapterPosition)
            }
        }
    }

    override fun getItemCount() = items.size
}