package com.dmon.rentalhere

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dmon.rentalhere.databinding.ItemReviewBinding
import com.dmon.rentalhere.model.ReviewResult

class ReviewRecyclerViewAdapter: RecyclerView.Adapter<ReviewRecyclerViewAdapter.ReviewViewHolder>() {
    private val reviewModelList = ArrayList<ReviewResult.ReviewModel>()
    private lateinit var listener: OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(holder: ReviewViewHolder, view: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val reviewModel = reviewModelList[position]
        holder.setItem(reviewModel)
        holder.setOnItemClickListener(listener)
    }

    override fun getItemCount(): Int = reviewModelList.size
    fun addItem(reviewModel: ReviewResult.ReviewModel) = reviewModelList.add(reviewModel)
    fun addAll(reviewModelList: ArrayList<ReviewResult.ReviewModel>){
        this.reviewModelList.clear()
        this.reviewModelList.addAll(reviewModelList)
    }
    fun clear() = reviewModelList.clear()
    fun getItem(position: Int) = reviewModelList[position]
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var listener: OnItemClickListener
        private var binding: ItemReviewBinding = ItemReviewBinding.bind(itemView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                    if(listener != null) listener.onItemClick(this, itemView, position)
            }
        }

        fun setItem(reviewModel: ReviewResult.ReviewModel){
            // todo : 프로필이미지 설정때 쓰기
//                Glide.with(itemView.context)
//                    .load(diaryModel.coverImageUrl)
//                    .apply(RequestOptions().centerCrop().override(250, 300))
//                    .into(binding.diaryItemCoverImageView)
            binding.idOrShopTextView.text = reviewModel.reviewerId
            binding.ratingBar.rating = reviewModel.reviewScore.toFloat()
            binding.reviewTextView.text = reviewModel.reviewText
            reviewModel.shopName?.let{
                binding.idOrShopTextView.run{ text = it; textSize = 20f; }
                binding.reviewIconImageView.visibility = View.VISIBLE
            }
            reviewModel.shopAddress?.let{ binding.addressTextView.run{ text = it; visibility = View.VISIBLE } }
            reviewModel.date?.let{ binding.dateTextView.run{ text = it; visibility = View.VISIBLE } }
        }

        fun setOnItemClickListener(listener: OnItemClickListener){
            this.listener = listener
        }
    }
}