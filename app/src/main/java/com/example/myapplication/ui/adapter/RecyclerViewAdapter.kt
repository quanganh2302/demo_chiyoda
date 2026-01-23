package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.helper.PackingLabel

class RecyclerViewAdapter(
    private val items: MutableList<PackingLabel>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

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

        holder.tvProductCode.text = item.itemCode ?: "-"
        holder.tvOrder.text = item.workOrderNo ?: "-"
        holder.tvDate.text = item.date?.toString() ?: "-"
        holder.tvQty.text = item.quantity.toString()

        holder.btnDelete.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onDelete(adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
