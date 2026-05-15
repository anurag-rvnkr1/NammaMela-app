package com.example.nammamela.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nammamela.data.CastEntity
import com.example.nammamela.databinding.ItemCastHorizontalBinding

class CastAdapter(private var castList: List<CastEntity>) :
    RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    class CastViewHolder(val binding: ItemCastHorizontalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val binding = ItemCastHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        val member = castList[position]
        holder.binding.tvCastName.text = member.name
        holder.binding.tvCastRole.text = member.role
        Glide.with(holder.itemView.context)
            .load(member.imageUrl)
            .circleCrop()
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(holder.binding.ivCastImage)
    }

    override fun getItemCount() = castList.size

    fun updateList(newList: List<CastEntity>) {
        castList = newList
        notifyDataSetChanged()
    }
}
