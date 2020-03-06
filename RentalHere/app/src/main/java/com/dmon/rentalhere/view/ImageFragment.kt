package com.dmon.rentalhere.view


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.dmon.rentalhere.R
import com.dmon.rentalhere.databinding.FragmentImageBinding

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val URL_KEY = "urlKey"

class ImageFragment : Fragment() {
    private var imageUrl: String? = null
    private lateinit var binding: FragmentImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageUrl = it.getString(URL_KEY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_image, container, false)
        Glide.with(context!!)
            .load(imageUrl)
            .apply(RequestOptions().centerCrop())
            .into(binding.imageView)
        return binding.root
    }


    companion object {
        @JvmStatic
        fun newInstance(imageUrl: String) =
            ImageFragment().apply {
                arguments = Bundle().apply {
                    putString(URL_KEY, imageUrl)
                }
            }
    }
}
