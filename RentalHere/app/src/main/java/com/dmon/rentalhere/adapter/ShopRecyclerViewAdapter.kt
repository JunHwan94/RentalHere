package com.dmon.rentalhere.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dmon.rentalhere.R
import com.dmon.rentalhere.databinding.ItemMyShopBinding
import com.dmon.rentalhere.model.ShopResult

class ShopRecyclerViewAdapter: RecyclerView.Adapter<ShopRecyclerViewAdapter.ShopViewHolder>() {
    private val shopModelList = ArrayList<ShopResult.ShopModel>()
    private lateinit var listener: OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(holder: ShopViewHolder, view: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_shop, parent, false)
        return ShopViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val shopModel = shopModelList[position]
        holder.setItem(shopModel)
        holder.setOnItemClickListener(listener)
    }

    override fun getItemCount(): Int = shopModelList.size
    fun addItem(shopModel: ShopResult.ShopModel) = shopModelList.add(shopModel)
    fun addAll(shopModelList: ArrayList<ShopResult.ShopModel>){
        this.shopModelList.clear()
        this.shopModelList.addAll(shopModelList)
    }
    fun clear() = shopModelList.clear()
    fun getItem(position: Int) = shopModelList[position]
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    class ShopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var listener: OnItemClickListener
        private var binding = ItemMyShopBinding.bind(itemView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if(listener != null) listener.onItemClick(this, itemView, position)
            }
        }

        fun setItem(shopModel: ShopResult.ShopModel){
            shopModel.mainPicNum.run{
                var url: String =
                    when(this){
                        "0" -> shopModel.shopProfileImageUrl1
                        "1" -> shopModel.shopProfileImageUrl2
                        "2" -> shopModel.shopProfileImageUrl3
                        "3" -> shopModel.shopProfileImageUrl4
                        "4" -> shopModel.shopProfileImageUrl5
                        else -> shopModel.shopProfileImageUrl1
                    }
                Glide.with(itemView.context)
                    .load(if(this == "") R.drawable.icon else url)
                    .apply(RequestOptions().centerCrop())
                    .into(binding.shopProfileImageView)
            }
            with(binding){
                shopNameTextView.text = shopModel.shopName
                addressTextView.text = shopModel.shopAddress
                telNumTextView.text = shopModel.shopTelNum
                descTextView.text = shopModel.shopItemKinds
            }
//            binding.also{
//                it.shopNameTextView.text = shopModel.shopName
//                it.addressTextView.text = shopModel.shopAddress
//                it.telNumTextView.text = shopModel.shopTelNum
//                it.descTextView.text = shopModel.shopItemKinds
//            }
        }

        fun setOnItemClickListener(listener: OnItemClickListener){
            this.listener = listener
        }
    }
}