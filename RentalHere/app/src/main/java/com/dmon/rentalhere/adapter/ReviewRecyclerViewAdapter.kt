package com.dmon.rentalhere.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.dmon.rentalhere.R
import com.dmon.rentalhere.databinding.ItemReviewBinding
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.model.ReviewResult
import com.dmon.rentalhere.retrofit.FIELD_REVIEW_IDX
import com.dmon.rentalhere.retrofit.RetrofitClient
import com.dmon.rentalhere.retrofit.RetrofitService
import com.dmon.rentalhere.view.MY_REVIEW_TYPE
import kotlinx.android.synthetic.main.item_review.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewRecyclerViewAdapter(val type: Int, val context: Context): RecyclerView.Adapter<ReviewRecyclerViewAdapter.ReviewViewHolder>() {
    private val reviewModelList = ArrayList<ReviewResult.ReviewModel>()
    private lateinit var listener: OnItemClickListener
    lateinit var holder: ReviewViewHolder
    private var position: Int = -1

    fun interface OnItemClickListener{
        fun onItemClick(holder: ReviewViewHolder, view: View, position: Int)
    }
    interface OnClickListener{
        fun onClick(holder: ReviewViewHolder, view: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view, type, context, this)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val reviewModel = reviewModelList[position]
        this.holder = holder
        this.position = position
        holder.setItem(reviewModel)
        holder.setOnItemClickListener(listener)
    }

    override fun getItemCount(): Int = reviewModelList.size
    fun getViewHolder() = holder
    fun getPosition() = position
    fun addItem(reviewModel: ReviewResult.ReviewModel) = reviewModelList.add(reviewModel)
    fun addAll(reviewModelList: ArrayList<ReviewResult.ReviewModel>){
        this.reviewModelList.clear()
        this.reviewModelList.addAll(reviewModelList)
    }
    fun removeItem(position: Int) = reviewModelList.removeAt(position)
    fun clear() = reviewModelList.clear()
    fun getItem(position: Int) = reviewModelList[position]
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    class ReviewViewHolder(itemView: View, type: Int, val context: Context, adapter: ReviewRecyclerViewAdapter) : RecyclerView.ViewHolder(itemView){
        private lateinit var listener: OnItemClickListener
        private var retrofitService: RetrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
        private var binding: ItemReviewBinding = ItemReviewBinding.bind(itemView)
        private var type: Int

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if(listener != null) listener.onItemClick(this, itemView, position)
            }
            itemView.deleteButton.setOnClickListener{
                context.run{
                    alert(message = getString(R.string.dialog_delete_review)) {
                        positiveButton(R.string.confirm) {
                            adapter.postDelete(position)
                        }
                        negativeButton(getString(R.string.cancel)){}
                    }.show()
                }
            }
            this.type = type
        }

        fun setItem(reviewModel: ReviewResult.ReviewModel){
            // 프로필이미지 설정때 쓰기
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

            when(type) {
                MY_REVIEW_TYPE -> binding.deleteButton.run {
                    visibility = View.VISIBLE
                }
            }
        }

        fun setOnItemClickListener(listener: OnItemClickListener){
            this.listener = listener
        }

        private fun ReviewRecyclerViewAdapter.postDelete(position: Int){
            val reviewIdx = getItem(position).reviewIdx!!
            val map = HashMap<String, Any>().apply{ this[FIELD_REVIEW_IDX] = reviewIdx }
            retrofitService.postDeleteReview(map).enqueue(object : Callback<BaseResult> {
                override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                    removeItem(position)
                    notifyDataSetChanged()
                    context.run{
                        toast(getString(R.string.toast_delete_review))
                    }
                }

                override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                    context.run{
                        toast(getString(R.string.toast_request_failed))
                    }
                }
            })
        }
    }
}