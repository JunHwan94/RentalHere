package com.dmon.rentalhere.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.dmon.rentalhere.R
import com.dmon.rentalhere.databinding.FragmentShopInfoBinding
import com.dmon.rentalhere.databinding.ItemReviewBinding
import com.dmon.rentalhere.model.ReviewResult
import com.dmon.rentalhere.model.ReviewResult.ReviewModel
import com.dmon.rentalhere.retrofit.*
import kotlinx.android.synthetic.main.fragment_shop_info.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.runOnUiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val SHOP_INFO_TAG = "ShopInfoFragment"
class ShopInfoFragment : Fragment(), AnkoLogger {
    override val loggerTag: String
        get() = SHOP_INFO_TAG
    private lateinit var binding: FragmentShopInfoBinding
    private var shopIdx: String? = null
    private var shopAddress: String? = null
    private var shopTelNum: String? = null
    private var shopInfo: String? = null
    private var shopProfileImageUrl: String? = null
    private lateinit var retrofitService: RetrofitService
    private lateinit var reviewAdapter: ReviewRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            shopIdx = it.getString(FIELD_SHOP_IDX)
            shopAddress = it.getString(FIELD_SHOP_ADDRESS)
            shopTelNum = it.getString(FIELD_SHOP_TEL_NUM)
            shopInfo = it.getString(FIELD_SHOP_INFO)
            shopProfileImageUrl = it.getString(FIELD_SHOP_MAIN_PICTURE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shop_info, container, false)
        setView()
        loadReview()
        return binding.root
    }

    /**
     * 리뷰 요청
     */
    private fun loadReview() {
        reviewAdapter = ReviewRecyclerViewAdapter()
        binding.recyclerView.apply{ adapter = reviewAdapter; layoutManager = LinearLayoutManager(context) }
        retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
        info(shopIdx)
        val map = HashMap<String, Any>().apply{ this[FIELD_SHOP_IDX] = shopIdx!! }
        retrofitService.postGetReview(map).enqueue(object : Callback<ReviewResult>{
            override fun onResponse(call: Call<ReviewResult>, response: Response<ReviewResult>) {
                val reviewResultItem = response.body()!!.reviewResultItem
                if(reviewResultItem.result == "Y") {
                    val reviewModelList = reviewResultItem.reviewModelList
                    GlobalScope.launch {
                        sequence{ yieldAll(reviewModelList) }
                            .take(2)
                            .forEach {
                                reviewAdapter.addItem(it)
                                runOnUiThread { reviewAdapter.notifyDataSetChanged() }
                            }
                    }
                }
            }

            override fun onFailure(call: Call<ReviewResult>, t: Throwable) {

            }
        })
    }

    /**
     * 뷰 설정
     */
    private fun setView(){
        binding.addrTextView.text = shopAddress
        binding.telNumTextView.text = shopTelNum
        binding.descTextView.text = shopInfo
        Glide.with(context!!)
            .load(shopProfileImageUrl)
            .apply(RequestOptions().centerCrop())
            .into(binding.shopProfileImageView)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    interface OnFragmentInteractionListener {
    }

    companion object {
        @JvmStatic
        fun newInstance(shopIdx: String, shopAddress: String, shopTelNum: String, shopInfo: String, shopProfileImageUrl: String) =
            ShopInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(FIELD_SHOP_IDX, shopIdx)
                    putString(FIELD_SHOP_ADDRESS, shopAddress)
                    putString(FIELD_SHOP_TEL_NUM, shopTelNum)
                    putString(FIELD_SHOP_INFO, shopInfo)
                    putString(FIELD_SHOP_PHOTO1_URL, shopProfileImageUrl)
                }
            }
    }

    class ReviewRecyclerViewAdapter: RecyclerView.Adapter<ReviewRecyclerViewAdapter.ReviewViewHolder>() {
        private val reviewModelList = ArrayList<ReviewModel>()
//        private lateinit var listener: OnItemClickListener

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
//            holder.setOnItemClickListener(listener)
        }

        override fun getItemCount(): Int = reviewModelList.size
        fun addItem(reviewModel: ReviewModel) = reviewModelList.add(reviewModel)
        fun clear() = reviewModelList.clear()
        fun getItem(position: Int) = reviewModelList[position]
//        fun setOnItemClickListener(listener: OnItemClickListener){
//            this.listener = listener
//        }

        class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            private lateinit var listener: OnItemClickListener
            private var binding: ItemReviewBinding = ItemReviewBinding.bind(itemView)

            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
//                    if(listener != null) listener.onItemClick(this, itemView, position)
                }
            }

            fun setItem(reviewModel: ReviewModel){
                // todo : 프로필이미지 설정때 쓰기
//                Glide.with(itemView.context)
//                    .load(diaryModel.coverImageUrl)
//                    .apply(RequestOptions().centerCrop().override(250, 300))
//                    .into(binding.diaryItemCoverImageView)
                binding.idTextView.text = reviewModel.reviewerId
                binding.ratingBar.rating = reviewModel.reviewScore.toFloat()
                binding.reviewTextView.text = reviewModel.reviewText
            }

//            fun setOnItemClickListener(listener: OnItemClickListener){
//                this.listener = listener
//            }
        }
    }
}