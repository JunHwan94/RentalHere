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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager

import com.dmon.rentalhere.R
import com.dmon.rentalhere.adapter.ReviewRecyclerViewAdapter
import com.dmon.rentalhere.databinding.FragmentShopInfoBinding
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.model.ReviewResult
import com.dmon.rentalhere.model.ShopResult
import com.dmon.rentalhere.presenter.CLIENT_TYPE
import com.dmon.rentalhere.presenter.OWNER_TYPE
import com.dmon.rentalhere.presenter.TYPE_KEY
import com.dmon.rentalhere.retrofit.FIELD_SHOP_IDX
import com.dmon.rentalhere.retrofit.*
import kotlinx.android.synthetic.main.fragment_shop_info.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val SHOP_INFO_TAG = "ShopInfoFragment"
const val REVIEW_LIST_KEY = "reviewListKey"
const val SHOP_IDX_KEY = "shopIdxKey"
const val WRITE_REVIEW_CODE = 100
const val EDIT_TYPE = 10
const val EDIT_SHOP_CODE = 300
const val EDIT_PIC_CODE = 301
const val SHOP_MODEL_KEY = "shopModelKey"
class ShopInfoFragment : Fragment(), AnkoLogger, View.OnClickListener {
    override val loggerTag: String get() = SHOP_INFO_TAG
    private lateinit var binding: FragmentShopInfoBinding
    private var userType: Int? = 0
    private lateinit var shopModel: ShopResult.ShopModel
    private lateinit var retrofitService: RetrofitService
    private lateinit var reviewAdapter: ReviewRecyclerViewAdapter
    private lateinit var reviewModelList: ArrayList<ReviewResult.ReviewModel>
    private var callback: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            shopModel = it.getParcelable(SHOP_MODEL_KEY)!!
            userType = it.getInt(TYPE_KEY)
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
        if(context is OnFragmentInteractionListener) callback = context
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    private fun init(){
        reviewAdapter = ReviewRecyclerViewAdapter(EVERY_REVIEW_TYPE, context!!)
            .apply{
                setOnItemClickListener(object : ReviewRecyclerViewAdapter.OnItemClickListener{
                    override fun onItemClick(holder: ReviewRecyclerViewAdapter.ReviewViewHolder, view: View, position: Int) {}
                })
            }
        binding.reviewRecyclerView.run{ adapter = reviewAdapter; layoutManager = LinearLayoutManager(context) }
        retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
    }

    /**
     * 리뷰 목록 요청
     */
    private fun loadReview() {
//        info(shopIdx)
        reviewAdapter.clear()
        val map = HashMap<String, Any>().apply{ this[FIELD_SHOP_IDX] = shopModel.shopIdx }
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
        binding.addressTextView.text = shopModel.shopAddress
        binding.telNumTextView.text = shopModel.shopTelNum
        binding.descTextView.text = shopModel.shopInfo
//        Glide.with(context!!)
//            .load(shopModel.shopProfileImageUrl)
//            .apply(RequestOptions().centerInside())
//            .into(binding.shopProfileImageView)
        if(userType == OWNER_TYPE) {
            binding.editPicturesButton.visibility = View.VISIBLE
            binding.bottomButton.text = getString(R.string.edit_shop_info)
            binding.deleteButton.visibility = View.VISIBLE
        }
        setImagePagerAdapter()
    }

    /**
     * 매장사진 뷰페이저로 보여주기
     */
    private fun setImagePagerAdapter() {
        val adapter = ListPagerAdapter(childFragmentManager)
        binding.imagePager.run{
            this.adapter = adapter
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    // todo : 아래 점 위치 바꾸기
                }
            })
        }
        addAdapterItems(adapter)
    }

    private fun addAdapterItems(adapter: ListPagerAdapter){
        GlobalScope.launch {
            adapter.addItem(ImageFragment.newInstance(shopModel.shopProfileImageUrl1))
            if(shopModel.shopProfileImageUrl2 != "") adapter.addItem(ImageFragment.newInstance(shopModel.shopProfileImageUrl2))
            if(shopModel.shopProfileImageUrl3 != "") adapter.addItem(ImageFragment.newInstance(shopModel.shopProfileImageUrl3))
            if(shopModel.shopProfileImageUrl4 != "") adapter.addItem(ImageFragment.newInstance(shopModel.shopProfileImageUrl4))
            if(shopModel.shopProfileImageUrl5 != "") adapter.addItem(ImageFragment.newInstance(shopModel.shopProfileImageUrl5))

            runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setViewListener() {
        binding.bottomButton.setOnClickListener(this)
        binding.allReviewsButton.setOnClickListener(this)
        binding.deleteButton.setOnClickListener(this)
        binding.editPicturesButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            // 리뷰 쓰기
            binding.bottomButton -> {
                when(userType){
                    CLIENT_TYPE -> startWriteReviewActivity() // 리뷰 쓰기
                    OWNER_TYPE -> startEditShopActivity()// 매장 정보 수정
                }
            }
            // 리뷰 목록
            binding.allReviewsButton -> startAllReviewActivity()
            binding.deleteButton -> deleteShop()
            binding.editPicturesButton -> startEditPicturesActivity()
        }
    }

    private fun startEditPicturesActivity() {
        startActivityForResult(Intent(context, EditPicturesActivity::class.java).apply{
            putExtra(SHOP_MODEL_KEY, shopModel)
        }, EDIT_PIC_CODE)
    }

    /**
     * 매장 삭제 요청
     */
    private fun deleteShop() {
        alert(getString(R.string.dialog_delete_shop)){
            positiveButton(getString(R.string.confirm)){
                val map = HashMap<String, Any>().apply{ this[FIELD_SHOP_IDX] = shopModel.shopIdx }
                retrofitService.postDeleteShop(map).enqueue(object : Callback<BaseResult>{
                    override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                        if(response.body()!!.baseModel.result == "Y"){
                            toast(getString(R.string.toast_delete_shop))
                            callback!!.loadShops()
                            callback!!.backPress()
                        }
                    }

                    override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                        error("요청 실패")
                    }
                })
            }
            negativeButton(getString(R.string.cancel)){}
        }.show()
    }

    private fun startWriteReviewActivity(){
        startActivityForResult(Intent(context, WriteReviewActivity::class.java).apply{
            putExtra(FIELD_SHOP_IDX, shopModel.shopIdx)
            putExtra(FIELD_USER_IDX, arguments!!.getString(FIELD_USER_IDX))
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }, WRITE_REVIEW_CODE)
    }

    private fun startEditShopActivity(){
        startActivityForResult(Intent(context, RegisterShopActivity::class.java).apply{
//            putExtra(TYPE_KEY, 10)  // TYPE_KEY로 값 안들어감 왜안들어가는지 모르겠음
            putExtra(SHOP_MODEL_KEY, shopModel)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }, EDIT_SHOP_CODE)
    }

    private fun startAllReviewActivity(){
        startActivity(Intent(context, AllReviewActivity::class.java).apply{
            putExtra(REVIEW_TYPE_KEY, EVERY_REVIEW_TYPE)
            putParcelableArrayListExtra(REVIEW_LIST_KEY, reviewModelList)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == WRITE_REVIEW_CODE && resultCode == RESULT_OK){
            loadReview()
        }
        if(requestCode == EDIT_SHOP_CODE && resultCode == RESULT_OK && data != null){
            callback!!.setShopName(data.getStringExtra(FIELD_SHOP_NAME)!!)
            addressTextView.text = data.getStringExtra(FIELD_SHOP_ADDRESS)
            telNumTextView.text = data.getStringExtra(FIELD_SHOP_TEL_NUM)
            descTextView.text = data.getStringExtra(FIELD_SHOP_INFO)
        }
        if(requestCode == EDIT_PIC_CODE && resultCode == RESULT_OK){
            loadShop()
            callback!!.loadShops()
        }
    }

    private fun loadShop() {
        info("loadShop 실행됨")
        val map = HashMap<String, Any>().apply{ this[FIELD_SHOP_IDX] = shopModel.shopIdx }
        retrofitService.postGetShop(map).enqueue(object : Callback<ShopResult>{
            override fun onResponse(call: Call<ShopResult>, response: Response<ShopResult>) {
                val shopModel = response.body()!!.shopModel
                if(shopModel.result == "Y"){
                    this@ShopInfoFragment.shopModel = shopModel
                    setView()
                }
            }

            override fun onFailure(call: Call<ShopResult>, t: Throwable) {
                error("요청 실패")
            }
        })
    }

    interface OnFragmentInteractionListener {
        fun loadShops()
        fun backPress()
        fun setShopName(shopName: String)
    }

    companion object {
        @JvmStatic
        fun newInstance(shopModel: ShopResult.ShopModel, userType: Int) =
            ShopInfoFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(SHOP_MODEL_KEY, shopModel)
                    putInt(TYPE_KEY, userType)
                }
            }
    }

    class ListPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private val fragmentList = ArrayList<Fragment>()
        init {
            fragmentList.clear()
        }

        fun addItem(item: Fragment) {
            if(!fragmentList.contains(item))
                fragmentList.add(item)
            notifyDataSetChanged()
        }

        fun clear(){
            fragmentList.clear()
            notifyDataSetChanged()
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int = fragmentList.size
    }
}