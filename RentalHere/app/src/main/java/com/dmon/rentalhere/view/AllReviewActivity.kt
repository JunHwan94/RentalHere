package com.dmon.rentalhere.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.dmon.rentalhere.R
import com.dmon.rentalhere.ReviewRecyclerViewAdapter
import com.dmon.rentalhere.model.ReviewResult
import kotlinx.android.synthetic.main.activity_find_user.backButton
import kotlinx.android.synthetic.main.fragment_shop_info.recyclerView

class AllReviewActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_review)
        setViewListener()
        setRecyclerView()
    }

    private fun setRecyclerView() {
        val adapter = ReviewRecyclerViewAdapter().apply {
            addAll(intent.getParcelableArrayListExtra<ReviewResult.ReviewModel>(REVIEW_LIST_KEY) as ArrayList)
            notifyDataSetChanged()
        }
        recyclerView.also{
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this)
        }
    }

    private fun setViewListener() {
        backButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            backButton -> finish()
        }
    }
}
