package com.dmon.rentalhere.view

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dmon.rentalhere.R
import com.dmon.rentalhere.model.UserInfoResult
import com.dmon.rentalhere.presenter.ID_KEY
import com.dmon.rentalhere.presenter.PREF_KEY
import com.dmon.rentalhere.retrofit.FIELD_USER_ID
import com.dmon.rentalhere.retrofit.FIELD_USER_IDX
import com.dmon.rentalhere.retrofit.RetrofitClient
import com.dmon.rentalhere.retrofit.RetrofitService
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_user_info.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

const val MAIN_TAG = "MainActivity"
class MainActivity : AppCompatActivity(), View.OnClickListener, AnkoLogger, NavigationView.OnNavigationItemSelectedListener, WebViewFragment.OnFragmentInteractionListener{
    override val loggerTag: String get() = MAIN_TAG
//    private lateinit var adapter: ListPagerAdapter
    private lateinit var retrofitService: RetrofitService
    private lateinit var shopInfoFragment: ShopInfoFragment
    private lateinit var distOrderFragment: WebViewFragment
    private lateinit var userModel: UserInfoResult.UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
        setContentView(R.layout.activity_main)

        setPermission()
        setViewListener()
    }

    /**
     *  탭레이아웃 설정
     */
    private fun setTabs() {
        distOrderFragment = WebViewFragment.newInstance()
        val recOrderFragment = WebViewFragment.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.container, distOrderFragment).commit()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab!!.position){
                    0 -> supportFragmentManager.beginTransaction().remove(recOrderFragment).commit()
                    1 -> supportFragmentManager.beginTransaction().add(R.id.container, recOrderFragment).commit()
                }
            }
        })
    }

    /**
     *  이벤트리스너 설정
     */
    private fun setViewListener() {
        menuButton.setOnClickListener(this)
        searchButton.setOnClickListener(this)
        navigationView.setNavigationItemSelectedListener(this)
        backButton.setOnClickListener(this)
    }

    /**
     *  내비게이션 뷰 프로필설정
     */
    private fun setNavigationView(id: String) {
        val header = navigationView.getHeaderView(0)
        header.idTextView.text = id
    }

    /**
     *  위치 권한 요청
     */
    private fun setPermission() {
        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener{
                override fun onPermissionGranted() {
                    info("위치 권한 허용")
//                    setViewPager()
                    setTabs()
                    loadUserInfo()
                }

                override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                    finish()
                }
            })
//            .setRationaleMessage("사용자의 위치가 지도에 표시되며, 주변의 업체 정보를 제공하기 위해 사용자의 위치 정보를 요청합니다")
            .setDeniedMessage(R.string.denied_gps)
            .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            .check()
    }

    /**
     * 사용자 정보 요청
     */
    private fun loadUserInfo() {
        val map = HashMap<String, Any>().apply{ this[FIELD_USER_ID] = intent.getStringExtra(ID_KEY)!! }
        retrofitService.postGetUser(map).enqueue(object : Callback<UserInfoResult> {
            override fun onResponse(call: Call<UserInfoResult>,response: Response<UserInfoResult>) {
                userModel = response.body()!!.userModel
                if(userModel.result == "Y"){
                    setNavigationView(userModel.userId)
                }
            }

            override fun onFailure(call: Call<UserInfoResult>, t: Throwable) {
                error("응답 실패")
            }

        })
    }

    /**
     *  클릭 이벤트
     */
    override fun onClick(v: View?) {
        when(v){
            menuButton -> moveDrawer()
            backButton -> onBackPressed()
        }
    }

    /**
     *  내비게이션 뷰 열기 / 닫기
     */
    private fun moveDrawer() {
        if(!drawer.isDrawerOpen(GravityCompat.END))
            drawer.openDrawer(GravityCompat.END)
        else drawer.closeDrawer(GravityCompat.END)

    }

    /**
     *  내비게이션 뷰 아이템 이벤트
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_modify_info -> toast("정보 수정")
            R.id.nav_review -> toast("리뷰")
            R.id.nav_log_out -> logOut()
        }
        return true
    }

    private fun logOut() {
        val editor = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE).edit()
        with(editor){
            clear()
            commit()
        }
        startActivity(Intent(this, LoginActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }

    override fun onBackPressed() {
        when{
            drawer.isDrawerOpen(GravityCompat.END) -> drawer.closeDrawer(GravityCompat.END)
            tabs.visibility != View.VISIBLE -> showMain()
//            viewPager.visibility != View.VISIBLE -> showMain()
            else -> super.onBackPressed()
        }
    }

    /**
     * 첫 화면으로 다시 설정
     */
    private fun showMain(){
        container2.visibility = View.GONE
        container2.removeAllViews()
        supportFragmentManager.beginTransaction().remove(shopInfoFragment)
//        supportFragmentManager.popBackStackImmediate()
        shopTextView.visibility = View.INVISIBLE
        backButton.visibility = View.INVISIBLE
        topImageView.visibility = View.VISIBLE
        searchButton.visibility = View.VISIBLE
        tabs.visibility = View.VISIBLE
//        viewPager.visibility = View.VISIBLE
//        supportFragmentManager.beginTransaction()/*.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)*/.replace(R.id.container, distOrderFragment).commit()

        container.visibility = View.VISIBLE
    }

    /**
     * frameLayout ShopInfoFragment 적용
     */
    override fun replaceFragment(fragment: Fragment, shopName: String) {
        fragment.arguments?.apply{
            putString(FIELD_USER_IDX, userModel.userIdx)
        }
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container2, fragment).commit()
        this.shopInfoFragment = fragment as ShopInfoFragment
        shopTextView.apply{ visibility = View.VISIBLE; text = shopName}
        backButton.visibility = View.VISIBLE
        topImageView.visibility = View.GONE
        searchButton.visibility = View.GONE
        tabs.visibility = View.GONE // 탭레이아웃 가리기
//        viewPager.visibility = View.GONE // 중첩된 프래그먼트에서 터치가 중복되어 뷰페이저 가리기

        container.visibility = View.GONE
        container2.visibility = View.VISIBLE
    }

//    class ListPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
//        private val fragmentList = ArrayList<Fragment>()
//        init {
//            fragmentList.clear()
//        }
//
//        fun addItem(item: Fragment) {
//            if(!fragmentList.contains(item))
//                fragmentList.add(item)
//            notifyDataSetChanged()
//        }
//
//        fun clear(){
//            fragmentList.clear()
//            notifyDataSetChanged()
//        }
//
//        override fun getItem(position: Int): Fragment {
//            return fragmentList[position]
//        }
//
//        override fun getCount(): Int = fragmentList.size
//    }

    /**
     *  뷰페이저 설정 (웹뷰 맵이동 할 때 불편해서 안쓰기로)
     */
//    private fun setViewPager() {
//        adapter = ListPagerAdapter(supportFragmentManager)
//        adapter.addItem(WebViewFragment.newInstance())
//        adapter.addItem(WebViewFragment.newInstance())
//        viewPager.offscreenPageLimit = 1
//
//        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
//        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
//            override fun onTabReselected(tab: TabLayout.Tab?) {}
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {}
//
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                viewPager.currentItem = tab!!.position
//            }
//        })
//        viewPager.adapter = adapter
//    }
}
