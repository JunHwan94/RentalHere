package com.dmon.rentalhere.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

const val OWNER_MAIN_TAG = "OwnerMainActivity"
const val REGISTER_SHOP_CODE = 102
class OwnerMainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AnkoLogger, ShopInfoFragment.OnFragmentInteractionListener{
    override val loggerTag: String get() = OWNER_MAIN_TAG
    private lateinit var shopAdapter: ShopRecyclerViewAdapter
    private lateinit var shopInfoFragment: ShopInfoFragment
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_main)

        setAdapter()
        loadUserInfo()
        setViewListener()
    }

    override fun onStop() {
        drawer.closeDrawer(GravityCompat.END)
        super.onStop()
    }

    /**
     * 어댑터, 리사이클러뷰 설정
     */
    private fun setAdapter() {
        GlobalScope.launch {
            shopAdapter = withContext(Dispatchers.Default) {
                ShopRecyclerViewAdapter().apply {
                    setOnItemClickListener(ShopRecyclerViewAdapter.OnItemClickListener { _, _, position -> setFragment(getItem(position)) })
                }
            }
            recyclerView.run {
                adapter = shopAdapter
                layoutManager = LinearLayoutManager(this@OwnerMainActivity)
            }
        }
    }

    /**
     * 업체 정보 화면
     */
    fun setFragment(shopModel: ShopResult.ShopModel){
        runOnUiThread {
            shopTextView.text = shopModel.shopName
            backButton.visibility = View.VISIBLE
            container.visibility = View.VISIBLE
        }
        shopInfoFragment = ShopInfoFragment.newInstance(shopModel, OWNER_TYPE)
        supportFragmentManager.beginTransaction().replace(R.id.container, shopInfoFragment).commit()
    }

    /**
     * 사용자 정보 요청
     */
    private fun loadUserInfo() {
        info("loadUserInfo called")
        val map = HashMap<String, Any>().apply{ this[FIELD_USER_ID] = intent.getStringExtra(ID_KEY)!! }
        retrofitService.postGetUser(map).enqueue(object : Callback<UserInfoResult> {
            override fun onResponse(call: Call<UserInfoResult>, response: Response<UserInfoResult>) {
                userModel = response.body()!!.userModel
                if(userModel.result == "Y"){
                    info("userIdx : ${userModel.userIdx}")
                    setNavigationView(userModel.userId)
                    loadMyShops()
                }
            }

            override fun onFailure(call: Call<UserInfoResult>, t: Throwable) {
                error( "요청 실패")
                toast(getString(R.string.toast_request_failed))
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
     * 내 업체 불러오기
     */
    private fun loadMyShops() {
        info("loadMyShops called")
        info("userIdx : ${userModel.userIdx}")
        val map = HashMap<String, Any>().apply{ this[FIELD_USER_IDX] = userModel.userIdx }
        retrofitService.postGetMyShops(map).enqueue(object : Callback<MyShopsResult>{
            override fun onResponse(call: Call<MyShopsResult>, response: Response<MyShopsResult>) {
                val shopResultItem = response.body()!!.myShopsResultItem
                if(shopResultItem.result == "Y"){
                    recyclerView.visibility = View.VISIBLE
                    shopAdapter.run{
                        addAll(shopResultItem.shopModelList)
                        notifyDataSetChanged()
                    }
                    addShopButton.visibility = View.GONE
                }else{
                    info(userModel.userIdx)
                    info("${shopResultItem.message} addShopButton 보이게함")
                    addShopButton.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<MyShopsResult>, t: Throwable) {
                error("실패")
                toast(getString(R.string.toast_request_failed))
            }
        })
    }

    /**
     * 업체 등록
     */
    private fun startRegisterShopActivity() {
        info("startRegisterShopActivity called")
        startActivityForResult(Intent(this, RegisterShopActivity::class.java).apply{
            putExtra(FIELD_USER_IDX, userModel.userIdx)
        }, REGISTER_SHOP_CODE)
    }

    /**
     *  내비게이션 뷰 아이템 이벤트
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_edit_info -> {
                editUser(userModel, OWNER_TYPE)
            }
            R.id.nav_register_shop -> {
                startRegisterShopActivity()
            }
            R.id.nav_log_out -> {
                logOut()
            }
        }
        return true
    }

    /**
     * 첫 화면으로 다시 설정
     */
    private fun showMain() {
        shopTextView.text = getString(R.string.my_shop_list)
        backButton.visibility = View.GONE
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

    override fun loadShops(){
        info("loadShops 실행됨")
//        addShopButton.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        loadMyShops()
    }

    override fun backPress() = onBackPressed()

    override fun setShopName(shopName: String) {
        shopTextView.text = shopName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REGISTER_SHOP_CODE && resultCode == Activity.RESULT_OK){
            loadMyShops()
        }
    }

    override fun onClick(v: View?) {
        when(v){
            menuButton -> moveDrawer()
            backButton -> onBackPressed()
            addShopButton -> startRegisterShopActivity()
        }
    }

    override fun onBackPressed() {
        val toast = Toast.makeText(this, getString(R.string.toast_finish_on_twice_pressed), Toast.LENGTH_SHORT)
        when {
            drawer.isDrawerOpen(GravityCompat.END) -> drawer.closeDrawer(GravityCompat.END)
            container.visibility == View.VISIBLE -> showMain()
            else -> {
                if (System.currentTimeMillis() > backPressedTime + 2000) {
                    backPressedTime = System.currentTimeMillis()
                    toast.show()
                    return
                }
                if (System.currentTimeMillis() <= backPressedTime + 2000) {
                    super.onBackPressed()
                    toast.cancel()
                }
            }
        }
    }
}
