package com.example.writersassistant.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.writersassistant.R
import com.example.writersassistant.models.ImageItem

class ImageAdapter(
    private val onLongClickListener: (ImageItem) -> Unit
) : ListAdapter<ImageItem, ImageAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<ImageItem>() {
        override fun areItemsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(itemView: View, private val onLongClickListener: (ImageItem) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)

        fun bindData(item: ImageItem) {
            Glide.with(itemView)
                .load(item.url)
                .into(imageView)

            itemView.setOnLongClickListener {
                onLongClickListener(item)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item_layout, parent, false)
        return ViewHolder(view, onLongClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageItem = getItem(position)
        holder.bindData(imageItem)
    }
}
