package com.dmon.rentalhere.view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.dmon.rentalhere.R
import com.dmon.rentalhere.databinding.FragmentShopInfoBinding
import com.dmon.rentalhere.retrofit.*
import kotlinx.android.synthetic.main.fragment_shop_info.*

class ShopInfoFragment : Fragment() {
    private lateinit var binding: FragmentShopInfoBinding
    private var shopIdx: String? = null
    private var shopAddress: String? = null
    private var shopTelNum: String? = null
    private var shopInfo: String? = null
    private var shopProfileImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            shopIdx = it.getString(FIELD_SHOP_IDX)
            shopAddress = it.getString(FIELD_SHOP_ADDRESS)
            shopTelNum = it.getString(FIELD_SHOP_TEL_NUM)
            shopInfo = it.getString(FIELD_SHOP_INFO)
            shopProfileImageUrl = it.getString(FIELD_SHOP_PHOTO1_URL)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shop_info, container, false)
        binding.addrTextView.text = shopAddress
        binding.telNumTextView.text = shopTelNum
        binding.descTextView.text = shopInfo
        Glide.with(context!!)
            .load(shopProfileImageUrl)
            .apply(RequestOptions().centerCrop())
            .into(binding.shopProfileImageView)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    interface OnFragmentInteractionListener {
        fun setToolbarShopName(shopName: String) // 상단 매장명 메인액티비티에 구현하기
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
}
