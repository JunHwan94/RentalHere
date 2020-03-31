package com.dmon.rentalhere.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.dmon.rentalhere.BaseActivity
import com.dmon.rentalhere.R
import com.dmon.rentalhere.model.ReviewResult
import com.dmon.rentalhere.model.UserInfoResult
import com.dmon.rentalhere.presenter.CLIENT_TYPE
import com.dmon.rentalhere.presenter.ID_KEY
import com.dmon.rentalhere.presenter.TYPE_KEY
import com.dmon.rentalhere.retrofit.FIELD_USER_ID
import com.dmon.rentalhere.retrofit.FIELD_USER_IDX
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_client_main.*
import kotlinx.android.synthetic.main.activity_client_main.backButton
import kotlinx.android.synthetic.main.activity_client_main.container
import kotlinx.android.synthetic.main.activity_client_main.container2
import kotlinx.android.synthetic.main.activity_client_main.drawer
import kotlinx.android.synthetic.main.activity_client_main.menuButton
import kotlinx.android.synthetic.main.activity_client_main.navigationView
import kotlinx.android.synthetic.main.activity_client_main.shopTextView
import kotlinx.android.synthetic.main.activity_client_main.topImageView
import kotlinx.android.synthetic.main.activity_owner_main.*
import kotlinx.android.synthetic.main.nav_header_user_info.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

const val MAIN_TAG = "ClientMainActivity"
const val USER_MODEL_KEY = "userModelKey"
const val EDIT_USER_CODE = 101
const val REVIEW_TYPE_KEY = "reviewTypeKey"
const val MY_REVIEW_TYPE = 1
const val EVERY_REVIEW_TYPE = 2
const val DIST_TYPE = 1
const val REC_TYPE = 2
const val SEARCH_TYPE= 3
class ClientMainActivity : BaseActivity(), View.OnClickListener, AnkoLogger, NavigationView.OnNavigationItemSelectedListener, WebViewFragment.OnFragmentInteractionListener{
    override val loggerTag: String get() = MAIN_TAG
//    private lateinit var adapter: ListPagerAdapter
    private lateinit var shopInfoFragment: ShopInfoFragment
    private lateinit var distOrderFragment: WebViewFragment
    private lateinit var searchFragment: WebViewFragment
    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_main)

        setPermission()
        setViewListener()
    }

    override fun onStop() {
        drawer.closeDrawer(GravityCompat.END)
        super.onStop()
    }

    /**
     *  탭레이아웃 설정
     */
    private fun setTabs() {
        distOrderFragment = WebViewFragment.newInstance(DIST_TYPE)
        val recOrderFragment = WebViewFragment.newInstance(REC_TYPE)
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
        header.idOrShopTextView.text = "${id}님 안녕하세요."
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
                    when{
                        intent.getStringExtra(ID_KEY) ?: "" == "" -> setNavigationViewWithoutLogin()
                        else -> loadUserInfo()
                    }
//                    intent.getStringExtra(ID_KEY)?.let{ loadUserInfo() }
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

    private fun setNavigationViewWithoutLogin() {
        userModel = null
        navigationView.run{
            getHeaderView(0).idOrShopTextView.text = getString(R.string.need_login)
            menu.run {
                getItem(0).run {
                    title = "회원가입"
                    icon = getDrawable(R.drawable.ic_enroll)
                }
                getItem(1).run{
                    title = "로그인"
                    icon = getDrawable(R.drawable.ic_person_outline_black_24dp)
                }
                getItem(2).isVisible = false
            }
            setNavigationItemSelectedListener{item ->
                when(item.itemId){
                    R.id.nav_edit_info -> startActivity(Intent(this@ClientMainActivity, TermsActivity::class.java).apply{
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    })
                    R.id.nav_review -> startActivity(Intent(this@ClientMainActivity, LoginActivity::class.java).apply{
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(TYPE_KEY, CLIENT_TYPE)
                    }).also{
                        finish()
                    }
                }
                true
            }
        }
    }

    /**
     * 사용자 정보 요청
     */
    private fun loadUserInfo() {
        val map = HashMap<String, Any>().apply{ this[FIELD_USER_ID] = intent.getStringExtra(ID_KEY)!! }
        retrofitService.postGetUser(map).enqueue(object : Callback<UserInfoResult> {
            override fun onResponse(call: Call<UserInfoResult>,response: Response<UserInfoResult>) {
                userModel = response.body()!!.userModel
                if(userModel!!.result == "Y"){
                    setNavigationView(userModel!!.userId)
                }
            }

            override fun onFailure(call: Call<UserInfoResult>, t: Throwable) {
                error("요청 실패")
                toast(getString(R.string.toast_request_failed))
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
            searchButton -> showSearchFragmentInContainer3()
        }
    }

    /**
     *  내비게이션 뷰 아이템 이벤트
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_edit_info -> {
                editUser(userModel!!, CLIENT_TYPE)
            }
            R.id.nav_review -> {
                showAllReview()
            }
            R.id.nav_log_out -> {
                logOut()
            }
        }
        return true
    }

    /**
     * 내 리뷰 보기 (AllReviewActivity 실행)
     */
    private fun showAllReview() {
        val map = HashMap<String, Any>().apply{ this[FIELD_USER_IDX] = userModel!!.userIdx }
        retrofitService.postGetMyReview(map).enqueue(object : Callback<ReviewResult>{
            override fun onResponse(call: Call<ReviewResult>, response: Response<ReviewResult>) {
                val reviewResultItem = response.body()!!.reviewResultItem
                if(reviewResultItem.result == "Y") {
                    startActivity(
                        Intent(
                            this@ClientMainActivity,
                            AllReviewActivity::class.java
                        ).apply {
                            putExtra(REVIEW_TYPE_KEY, MY_REVIEW_TYPE)
                            reviewResultItem.reviewModelList.let {
                                it.reverse()
                                putParcelableArrayListExtra(REVIEW_LIST_KEY, it)
                            }
                        })
                }else{
                    startActivity(Intent(this@ClientMainActivity, AllReviewActivity::class.java).apply{
                        putExtra(REVIEW_TYPE_KEY, MY_REVIEW_TYPE)
                    })
                }
            }

            override fun onFailure(call: Call<ReviewResult>, t: Throwable) {
                toast(getString(R.string.toast_request_failed))
            }
        })
    }

    override fun onBackPressed() {
        val toast = Toast.makeText(this, getString(R.string.toast_finish_on_twice_pressed), Toast.LENGTH_SHORT)
        when{
            drawer.isDrawerOpen(GravityCompat.END) -> drawer.closeDrawer(GravityCompat.END)
            shopTextView.text != getString(R.string.search_location) && container3.visibility == View.VISIBLE -> showSearchFragment()
            tabs.visibility != View.VISIBLE -> showMain()
//            viewPager.visibility != View.VISIBLE -> showMain()
            else -> {
                if(System.currentTimeMillis() > backPressedTime + 2000){
                    backPressedTime = System.currentTimeMillis()
                    toast.show()
                    return
                }
                if(System.currentTimeMillis() <= backPressedTime + 2000){
                    super.onBackPressed()
                    toast.cancel()
                }
            }
        }
    }

    private fun showSearchFragment(){
        shopTextView.text = getString(R.string.search_location)
        supportFragmentManager.beginTransaction().remove(shopInfoFragment).commit()
    }

    /**
     * 첫 화면으로 다시 설정
     */
    private fun showMain(){
        container2.visibility = View.GONE
        container2.removeAllViews()
        container3.visibility = View.GONE
        container3.removeAllViews()
        shopTextView.visibility = View.INVISIBLE
        backButton.visibility = View.INVISIBLE
        topImageView.visibility = View.VISIBLE
        searchButton.visibility = View.VISIBLE
        tabs.visibility = View.VISIBLE
//        viewPager.visibility = View.VISIBLE

        container.visibility = View.VISIBLE
    }

    /**
     * frameLayout container2에 ShopInfoFragment 적용
     */
    override fun showShopInfoFragmentInContainer2(fragment: Fragment, shopName: String) {
        fragment.arguments?.run{
//            userModel?.let{
                putString(FIELD_USER_IDX, userModel?.userIdx ?: "-1")
//            }
        }
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.container2, fragment).commit()
        this.shopInfoFragment = fragment as ShopInfoFragment
        shopTextView.run{ visibility = View.VISIBLE; text = shopName}
        hideMain()
        container2.visibility = View.VISIBLE
    }

    /**
     * container3에 SearchFragment 적용
     */
    private fun showSearchFragmentInContainer3(){
        searchFragment = WebViewFragment.newInstance(SEARCH_TYPE)
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.container3, searchFragment).commit()
        hideMain()
        shopTextView.run{ visibility = View.VISIBLE; text = getString(R.string.search_location) }
        container3.visibility = View.VISIBLE
    }

    private fun hideMain(){
        backButton.visibility = View.VISIBLE
        topImageView.visibility = View.GONE
        searchButton.visibility = View.GONE
        tabs.visibility = View.GONE // 탭레이아웃 가리기
//        viewPager.visibility = View.GONE // 중첩된 프래그먼트에서 터치가 중복되어 뷰페이저 가리기

        container.visibility = View.GONE
    }

    /**
     * container3에 ShopInfoFragment 적용
     */
    override fun showShopInfoFragmentInContainer3(fragment: Fragment, shopName: String) {
        fragment.arguments?.run{
//            userModel?.let {
                putString(FIELD_USER_IDX, userModel?.userIdx ?: "-1")
//            }
        }
        supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out).add(R.id.container3, fragment).commit()
        this.shopInfoFragment = fragment as ShopInfoFragment
        shopTextView.run{ visibility = View.VISIBLE; text = shopName}
        topImageView.visibility = View.GONE
    }

    override fun showFailedToast() {
        toast(getString(R.string.toast_request_failed))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == EDIT_USER_CODE && resultCode == Activity.RESULT_OK){
            data?.let {
                it.getParcelableExtra<UserInfoResult.UserModel>(USER_MODEL_KEY)?.let{ userModel ->
                    this.userModel = userModel
                    info("${userModel.userId}")
                    info("${userModel.userName}")
                    info("${userModel.userEmail}")
                    info("${userModel.userCpNum}")
                }
            }
        }
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
