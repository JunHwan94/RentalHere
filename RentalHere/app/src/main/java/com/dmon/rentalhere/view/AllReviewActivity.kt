package com.dmon.rentalhere.view

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dmon.rentalhere.BaseActivity
import com.dmon.rentalhere.R
import com.dmon.rentalhere.adapter.ReviewRecyclerViewAdapter
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.model.ReviewResult
import com.dmon.rentalhere.retrofit.FIELD_REVIEW_IDX
import kotlinx.android.synthetic.main.activity_all_review.*
import kotlinx.android.synthetic.main.fragment_shop_info.recyclerView
import kotlinx.android.synthetic.main.item_review.*
import kotlinx.android.synthetic.main.item_review.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val ALL_REVIEW_TAG = "AllReviewActivity"
class AllReviewActivity : BaseActivity(), View.OnClickListener, AnkoLogger {
    override val loggerTag: String get() = ALL_REVIEW_TAG
    private var reviewType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_review)

        init()
        setViewListener()
        setRecyclerView()
    }

    private fun init(){
        reviewType = intent.getIntExtra(REVIEW_TYPE_KEY, 0)
        when(reviewType){
            MY_REVIEW_TYPE -> topTextView.run{ text = getString(R.string.my) + " " + text }
        }
    }

    private fun setRecyclerView() {
        val adapter = ReviewRecyclerViewAdapter(reviewType, this).apply {
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
