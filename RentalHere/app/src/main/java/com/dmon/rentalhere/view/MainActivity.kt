package com.dmon.rentalhere.view

import android.Manifest
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
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_user_info.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import java.util.*

const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), View.OnClickListener, AnkoLogger, NavigationView.OnNavigationItemSelectedListener, WebViewFragment.OnFragmentInteractionListener{
    override val loggerTag: String get() = TAG
    private lateinit var adapter: ListPagerAdapter
    private lateinit var shopInfoFragment: ShopInfoFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setPermission()
        setNavigationView()
        setViewListener()
    }

    /**
     *  뷰페이저 설정
     */
    private fun setViewPager() {
        adapter = ListPagerAdapter(supportFragmentManager)
        adapter.addItem(WebViewFragment.newInstance())
        adapter.addItem(WebViewFragment.newInstance())
        viewPager.offscreenPageLimit = 1

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position
            }
        })
        viewPager.adapter = adapter
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
    private fun setNavigationView() {
        val header = navigationView.getHeaderView(0)
        header.idTextView.text = "테스트아이디"
        Glide.with(this)
            .load(getDrawable(R.drawable.logo_blue))
            .apply(RequestOptions().circleCrop().fitCenter())
            .into(header.profileImageView)
    }

    /**
     *  위치 권한 요청
     */
    private fun setPermission() {
        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener{
                override fun onPermissionGranted() {
                    info("위치 권한 허용")
                    setViewPager()
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
            R.id.nav_log_out -> toast("로그아웃")
        }
        return true
    }

    override fun onBackPressed() {
        when{
            drawer.isDrawerOpen(GravityCompat.END) -> drawer.closeDrawer(GravityCompat.END)
            viewPager.visibility != View.VISIBLE -> showMain()
            else -> super.onBackPressed()
        }
    }

    private fun showMain(){
        shopTextView.visibility = View.INVISIBLE
        topImageView.visibility = View.VISIBLE
        searchButton.visibility = View.VISIBLE
        tabs.visibility = View.VISIBLE
        viewPager.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).remove(shopInfoFragment).commit()
    }

    /**
     * frameLayout ShopInfoFragment로 변경
     */
    override fun replaceFragment(fragment: Fragment, shopName: String) {
        this.shopInfoFragment = fragment as ShopInfoFragment
        shopTextView.apply{ visibility = View.VISIBLE; text = shopName}
        topImageView.visibility = View.GONE
        searchButton.visibility = View.GONE
        tabs.visibility = View.GONE // 탭레이아웃 가리기
        viewPager.visibility = View.GONE // 중첩된 프래그먼트에서 터치가 중복되어 뷰페이저 가리기
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container, fragment).commit()
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
