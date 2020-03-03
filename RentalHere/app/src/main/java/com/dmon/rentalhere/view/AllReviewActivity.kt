package com.dmon.rentalhere.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dmon.rentalhere.R
import com.dmon.rentalhere.adapter.ReviewRecyclerViewAdapter
import com.dmon.rentalhere.model.ReviewResult
import kotlinx.android.synthetic.main.activity_all_review.*
import kotlinx.android.synthetic.main.fragment_shop_info.recyclerView

class AllReviewActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_review)

        when(intent.getIntExtra(REVIEW_TYPE_KEY, 0)){
            MY_REVIEW_TYPE -> topTextView.run{ text = getString(R.string.my) + " " + text }
        }
        setViewListener()
        setRecyclerView()
    }

    private fun setRecyclerView() {
        val adapter = ReviewRecyclerViewAdapter().apply {
            val reviewModelList = intent.getParcelableArrayListExtra<ReviewResult.ReviewModel>(REVIEW_LIST_KEY) ?: ArrayList<ReviewResult.ReviewModel>()
            if(reviewModelList.size == 0) run {
                noReviewsTextView.visibility = View.VISIBLE
                recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            }
            else addAll(reviewModelList)
            notifyDataSetChanged()
        }
        recyclerView.run{
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@AllReviewActivity)
        }

        adapter.setOnItemClickListener(object : ReviewRecyclerViewAdapter.OnItemClickListener{
            override fun onItemClick(holder: ReviewRecyclerViewAdapter.ReviewViewHolder, view: View, position: Int) {
                when(intent.getIntExtra(REVIEW_TYPE_KEY, 0)){

                }
            }
        })
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
