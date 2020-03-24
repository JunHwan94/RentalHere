package com.dmon.rentalhere.view

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dmon.rentalhere.BaseActivity
import com.dmon.rentalhere.R
import com.dmon.rentalhere.adapter.ReviewRecyclerViewAdapter
import com.dmon.rentalhere.model.ReviewResult
import kotlinx.android.synthetic.main.activity_all_review.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger

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
                reviewRecyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            }
            else addAll(reviewModelList)
            notifyDataSetChanged()
        }
        reviewRecyclerView.run{
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@AllReviewActivity)
        }

        adapter.setOnItemClickListener(ReviewRecyclerViewAdapter.OnItemClickListener { _, _, _ -> })

        adapter.runAdapterWatchRoutine()
    }

    private fun ReviewRecyclerViewAdapter.runAdapterWatchRoutine() {
        if(0 < itemCount)
            GlobalScope.launch{
                while(true){
                    delay(200)
                    if(itemCount == 0) runOnUiThread { noReviewsTextView.visibility = View.VISIBLE }
                }
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
