package com.example.myapplication.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemPackingBinding
import com.example.myapplication.models.Box

class PackingAdapter(
    private val items: MutableList<Box>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<PackingAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPackingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPackingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.etProductPerBox.setText(item.numberBox.toString())
        holder.binding.etBoxCount.setText(item.count.toString())

        holder.binding.btnDelete.setOnClickListener {
            onDelete(position)
        }
    }

    override fun getItemCount(): Int = items.size
}
