package com.dmon.rentalhere.view

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.dmon.rentalhere.R
import com.dmon.rentalhere.ReviewRecyclerViewAdapter
import com.dmon.rentalhere.databinding.FragmentShopInfoBinding
import com.dmon.rentalhere.model.ReviewResult
import com.dmon.rentalhere.retrofit.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.support.v4.runOnUiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val SHOP_INFO_TAG = "ShopInfoFragment"
const val IMAGE_BASE_URL = "https://softer013.cafe24.com/uploads/"
const val REVIEW_LIST_KEY = "reviewListKey"
const val WRITE_REVIEW_CODE = 100
class ShopInfoFragment : Fragment(), AnkoLogger, View.OnClickListener {

    override val loggerTag: String
        get() = SHOP_INFO_TAG
    private lateinit var binding: FragmentShopInfoBinding
    private var shopIdx: String? = null
    private var shopName: String? = null
    private var shopAddress: String? = null
    private var shopTelNum: String? = null
    private var shopInfo: String? = null
    private var shopProfileImageUrl: String? = null
    private lateinit var retrofitService: RetrofitService
    private lateinit var reviewAdapter: ReviewRecyclerViewAdapter
    private lateinit var reviewModelList: ArrayList<ReviewResult.ReviewModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            shopIdx = it.getString(FIELD_SHOP_IDX)
            shopName = it.getString(FIELD_SHOP_NAME)
            shopAddress = it.getString(FIELD_SHOP_ADDRESS)
            shopTelNum = it.getString(FIELD_SHOP_TEL_NUM)
            shopInfo = it.getString(FIELD_SHOP_INFO)
            shopProfileImageUrl = it.getString(FIELD_SHOP_PHOTO1_URL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shop_info, container, false)
        init()
        setView()
        loadReview()
        setViewListener()
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun init(){
        reviewAdapter = ReviewRecyclerViewAdapter().apply{ setOnItemClickListener(object : ReviewRecyclerViewAdapter.OnItemClickListener{
            override fun onItemClick(holder: ReviewRecyclerViewAdapter.ReviewViewHolder, view: View, position: Int) {}
        }) }
        binding.recyclerView.run{ adapter = reviewAdapter; layoutManager = LinearLayoutManager(context) }
        retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
    }

    private fun setViewListener() {
        binding.writeReviewButton.setOnClickListener(this)
        binding.allReviewsButton.setOnClickListener(this)
    }

    /**
     * 리뷰 목록 요청
     */
    private fun loadReview() {
//        info(shopIdx)
        reviewAdapter.clear()
        val map = HashMap<String, Any>().apply{ this[FIELD_SHOP_IDX] = shopIdx!! }
        retrofitService.postGetReview(map).enqueue(object : Callback<ReviewResult>{
            override fun onResponse(call: Call<ReviewResult>, response: Response<ReviewResult>) {
                val reviewResultItem = response.body()!!.reviewResultItem
                if(reviewResultItem.result == "Y") { // 리뷰 있을 때
                    reviewModelList = reviewResultItem.reviewModelList
                    GlobalScope.launch {
                        sequence{ yieldAll(reviewModelList) }
                            .take(2)
                            .forEach {
                                reviewAdapter.addItem(it)
                                runOnUiThread { reviewAdapter.notifyDataSetChanged() }
                            }
                    }
                    binding.userReviewTextView.run { text = "${getString(R.string.user_review)}  ${reviewModelList.size}" }
                    binding.allReviewsButton.visibility = View.VISIBLE
                }else  // 리뷰 없을 때
                    binding.userReviewTextView.text = getString(R.string.no_review)
            }

            override fun onFailure(call: Call<ReviewResult>, t: Throwable) {
                error("요청 실패", t)
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
            .apply(RequestOptions().centerInside())
            .into(binding.shopProfileImageView)
    }

    override fun onClick(v: View?) {
        when(v){
            // 리뷰 쓰기
            binding.writeReviewButton -> {
                startActivityForResult(Intent(context, WriteReviewActivity::class.java).apply{
                    putExtra(FIELD_SHOP_IDX, shopIdx)
                    putExtra(FIELD_USER_IDX, arguments!!.getString(FIELD_USER_IDX))
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }, WRITE_REVIEW_CODE)
            }
            // 리뷰 목록
            binding.allReviewsButton-> {
                startActivity(Intent(context, AllReviewActivity::class.java).apply{
                    putParcelableArrayListExtra(REVIEW_LIST_KEY, reviewModelList)
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == WRITE_REVIEW_CODE && resultCode == RESULT_OK){
            loadReview()
        }
    }

    interface OnFragmentInteractionListener {
    }

    companion object {
        @JvmStatic
        fun newInstance(shopIdx: String, shopName: String, shopAddress: String, shopTelNum: String, shopInfo: String, shopProfileImageUrl: String) =
            ShopInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(FIELD_SHOP_IDX, shopIdx)
                    putString(FIELD_SHOP_NAME, shopName)
                    putString(FIELD_SHOP_ADDRESS, shopAddress)
                    putString(FIELD_SHOP_TEL_NUM, shopTelNum)
                    putString(FIELD_SHOP_INFO, shopInfo)
                    putString(FIELD_SHOP_PHOTO1_URL, shopProfileImageUrl)
                }
            }
    }
}