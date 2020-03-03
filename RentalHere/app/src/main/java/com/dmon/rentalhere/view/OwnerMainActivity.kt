package com.dmon.rentalhere.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dmon.rentalhere.BaseActivity
import com.dmon.rentalhere.R
import com.dmon.rentalhere.adapter.ShopRecyclerViewAdapter
import com.dmon.rentalhere.model.MyShopsResult
import com.dmon.rentalhere.model.ShopResult
import com.dmon.rentalhere.model.UserInfoResult
import com.dmon.rentalhere.presenter.ID_KEY
import com.dmon.rentalhere.presenter.OWNER_TYPE
import com.dmon.rentalhere.retrofit.FIELD_USER_ID
import com.dmon.rentalhere.retrofit.FIELD_USER_IDX
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_owner_main.*
import kotlinx.android.synthetic.main.nav_header_user_info.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

const val OWNER_MAIN_TAG = "OwnerMainActivity"
class OwnerMainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AnkoLogger{
    override val loggerTag: String get() = OWNER_MAIN_TAG
    private lateinit var shopAdapter: ShopRecyclerViewAdapter
    private lateinit var shopInfoFragment: ShopInfoFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_main)

        setAdapter()
        loadUserInfo()
        setViewListener()
    }

    private fun setAdapter() {
        shopAdapter = ShopRecyclerViewAdapter().apply { setOnItemClickListener(object : ShopRecyclerViewAdapter.OnItemClickListener{
            override fun onItemClick(holder: ShopRecyclerViewAdapter.ShopViewHolder, view: View, position: Int) {
                setFragment(getItem(position))
            }
        }) }
        recyclerView.run{
            adapter = shopAdapter
            layoutManager = LinearLayoutManager(this@OwnerMainActivity)
        }
    }

    fun setFragment(shopModel: ShopResult.ShopModel){
        container.visibility = View.VISIBLE
        shopInfoFragment = ShopInfoFragment.newInstance(shopModel.shopIdx, shopModel.shopName, shopModel.shopAddress, shopModel.shopTelNum, shopModel.shopInfo, shopModel.shopProfileImageUrl, OWNER_TYPE)
        supportFragmentManager.beginTransaction().replace(R.id.container, shopInfoFragment).commit()
    }

    /**
     * 사용자 정보 요청
     */
    private fun loadUserInfo() {
        val map = HashMap<String, Any>().apply{ this[FIELD_USER_ID] = intent.getStringExtra(ID_KEY)!! }
        retrofitService.postGetUser(map).enqueue(object : Callback<UserInfoResult> {
            override fun onResponse(call: Call<UserInfoResult>, response: Response<UserInfoResult>) {
                userModel = response.body()!!.userModel
                if(userModel.result == "Y"){
                    setNavigationView(userModel.userId)
                    loadMyShops()
                    addShopButton.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<UserInfoResult>, t: Throwable) {
                error( "요청 실패")
            }

        })
    }

    /**
     *  내비게이션 뷰 프로필설정
     */
    private fun setNavigationView(id: String) {
        val header = navigationView.getHeaderView(0)
        header.idOrShopTextView.text = "${id}님 안녕하세요."
    }

    /**
     * 내 매장 불러오기
     */
    private fun loadMyShops() {
        val map = HashMap<String, Any>().apply{ this[FIELD_USER_IDX] = userModel.userIdx }
        retrofitService.postGetMyShops(map).enqueue(object : Callback<MyShopsResult>{
            override fun onResponse(call: Call<MyShopsResult>, response: Response<MyShopsResult>) {
                val shopResultItem = response.body()!!.myShopsResultItem
                if(shopResultItem.result == "Y"){
                    shopAdapter.run{
                        addAll(shopResultItem.shopModelList)
                        notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<MyShopsResult>, t: Throwable) {
                error("실패")
            }
        })
    }

    /**
     * 매장 등록
     */
    private fun startRegisterShopActivity() {
        startActivity(Intent(this, RegisterShopActivity::class.java).apply{
            putExtra(FIELD_USER_IDX, userModel.userIdx)
        })
    }

    /**
     *  내비게이션 뷰 아이템 이벤트
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_edit_info -> editUser(userModel)
            R.id.nav_register_shop -> startRegisterShopActivity()
            R.id.nav_log_out -> logOut()
        }
        return true
    }
    /**
     * 첫 화면으로 다시 설정
     */
    private fun showMain() {
        container.visibility = View.GONE
        container.removeAllViews()
        supportFragmentManager.beginTransaction().remove(shopInfoFragment)
    }

    /**
     *  이벤트리스너 설정
     */
    private fun setViewListener() {
        menuButton.setOnClickListener(this)
        navigationView.setNavigationItemSelectedListener(this)
        backButton.setOnClickListener(this)
        addShopButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            menuButton -> moveDrawer()
            backButton -> onBackPressed()
            addShopButton -> startRegisterShopActivity()
        }
    }

    override fun onBackPressed() {
        when{
            drawer.isDrawerOpen(GravityCompat.END) -> drawer.closeDrawer(GravityCompat.END)
            container.visibility == View.VISIBLE -> showMain()
            else -> super.onBackPressed()
        }
    }
}
