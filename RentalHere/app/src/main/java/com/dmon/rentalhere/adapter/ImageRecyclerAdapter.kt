package com.dmon.rentalhere.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dmon.rentalhere.R
import com.dmon.rentalhere.databinding.ItemImageBinding

class ImageRecyclerAdapter: RecyclerView.Adapter<ImageRecyclerAdapter.ImageViewHolder>() {
    private val uriList = ArrayList<String>()
    private lateinit var listener: OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(holder: ImageViewHolder, view: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = uriList[position]
        holder.setItem(uri)
        holder.setOnItemClickListener(listener)
    }

    override fun getItemCount(): Int = uriList.size
    fun addItem(uri: String){
        uriList.add(uri)
        if(uriList.size == 6) uriList.removeAt(0)
    }
    fun addAll(uriList: ArrayList<String>){
        this.uriList.clear()
        this.uriList.addAll(uriList)
    }
    fun removeItem(position: Int) = uriList.removeAt(position)
    fun clear() = uriList.clear()
    fun getItem(position: Int) = uriList[position]
    fun getItems() = uriList
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private lateinit var listener: OnItemClickListener
        private var binding = ItemImageBinding.bind(itemView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if(listener != null) listener.onItemClick(this, itemView, position)
            }
        }

        fun setItem(uri: String){
            if(uri != ""){
                Glide.with(itemView.context)
                    .load(Uri.parse(uri))
                    .apply(RequestOptions().centerCrop())
                    .into(binding.imageView)
//                binding.deleteButton.visibility = View.VISIBLE
            }
        }

        fun setOnItemClickListener(listener: OnItemClickListener){
            this.listener = listener
        }
    }
}