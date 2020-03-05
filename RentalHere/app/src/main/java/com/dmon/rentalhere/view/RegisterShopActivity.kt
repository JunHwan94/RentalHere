package com.dmon.rentalhere.view

import android.Manifest
import android.content.Intent
import android.database.Cursor
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Pair
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dmon.rentalhere.BaseActivity
import com.dmon.rentalhere.R
import com.dmon.rentalhere.adapter.ImageRecyclerAdapter
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.model.ShopResult
import com.dmon.rentalhere.retrofit.*
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_register_shop.*
import kotlinx.android.synthetic.main.activity_register_shop.backButton
import kotlinx.android.synthetic.main.activity_register_shop.topTextView
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import kotlin.collections.ArrayList

const val PICK_FROM_ALBUM_CODE = 200
const val REGISTER_SHOP_TAG = "RegisterShopActivity"
class RegisterShopActivity : BaseActivity(), AnkoLogger, View.OnClickListener {
    override val loggerTag: String get() = REGISTER_SHOP_TAG
    private lateinit var imageFile: File
    private lateinit var imageAdapter: ImageRecyclerAdapter
    private lateinit var userIdx: String
    private var shopModel: ShopResult.ShopModel? = null
//    private lateinit var fileList: ArrayList<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_shop)

        init()
        setViewListener()
    }

    private fun init(){
        intent.getParcelableExtra<ShopResult.ShopModel>(SHOP_MODEL_KEY)?.let{
            shopModel = it
            setView()
            // todo : 사진 처리하기
        }
        if(shopModel == null) {
            userIdx = intent.getStringExtra(FIELD_USER_IDX)!!
            setAdapter()
        }
        // TYPE_KEY로 값 안들어감.
//        if(registerType == EDIT_TYPE){
//            shopModel = intent.getParcelableExtra(SHOP_MODEL_KEY)!!
//            setView()
//        }else {
//            userIdx = intent.getStringExtra(FIELD_USER_IDX)!!
//            setAdapter()
//        }
    }

    private fun setView() {
        topTextView.text = getString(R.string.edit_shop_info)
        shopNameEditText.setText(shopModel!!.shopName)
        telEditText.setText(shopModel!!.shopTelNum)
        addressEditText.setText(shopModel!!.shopAddress)
        keywordEditText.setText(shopModel!!.shopKeyword)
        descEditText.setText(shopModel!!.shopInfo)
    }

    private fun setViewListener() {
        requestButton.setOnClickListener(this)
        backButton.setOnClickListener(this)
        telEditText.addTextChangedListener(cpWatcher)
    }

    private val cpWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if(s.toString().length > 11) {
                telEditText.setText(s.toString().dropLast(1))
                cpEditText.setSelection(11) // todo : 수정?
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun onClick(v: View?) {
        when(v){
            requestButton -> {
                when(shopModel){
                    null -> requestApproval()
                    else -> requestEdit()
                }
            }
            backButton -> onBackPressed()
        }
    }

    /**
     * 매장 정보 수정
     */
    private fun requestEdit() {
        val map = HashMap<String, Any>().apply{
            this[FIELD_SHOP_IDX] = shopModel!!.shopIdx
            this[FIELD_SHOP_NAME] = shopNameEditText.text.toString()
            this[FIELD_SHOP_TEL_NUM] = telEditText.text.toString()
            this[FIELD_SHOP_KEYWORD] = keywordEditText.text.toString()
            this[FIELD_SHOP_INFO] = descEditText.text.toString()
            this[FIELD_SHOP_ADDRESS] = addressEditText.text.toString()
            this[FIELD_SHOP_LAT] = getGeoCode().first.toString()
            this[FIELD_SHOP_LNG] = getGeoCode().second.toString()
        }
        retrofitService.postEditShop(map).enqueue(object : Callback<BaseResult>{
            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                if(response.body()!!.baseModel.result == "Y"){
                    toast(getString(R.string.toast_request_edit_complete))
                    setResult(RESULT_OK)
                    finish()
                }else toast(getString(R.string.toast_request_failed))
            }

            override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                error("요청 실패")
                t.printStackTrace()
            }
        })
    }

    /**
     * 매장 등록 요청
     */
    private fun requestApproval() {
        info("requestApproval실행됨")
        val userIdx = getRequestBody(userIdx)
        val shopName = getRequestBody(shopNameEditText.text.toString())
        val shopTelNum = getRequestBody(telEditText.text.toString())
        val shopKeyword = getRequestBody(keywordEditText.text.toString())
        val shopInfo = getRequestBody(descEditText.text.toString())
        val shopAddress = getRequestBody(addressEditText.text.toString())
        val shopLatitude = getRequestBody(getGeoCode().first!!)
        val shopLongitude = getRequestBody(getGeoCode().second!!)
        val partList = ArrayList<MultipartBody.Part?>(5)

        // 사진 파일 처리, 매장 등록 요청
        GlobalScope.launch {
            info("Coroutine 실행됨")
            while(imageAdapter.itemCount < 5){
                imageAdapter.addItem("")
            }
            var i = 0
            do{
                info(imageAdapter.getItem(i))
                val file = File(getRealPathFromURI(Uri.parse(imageAdapter.getItem(i))))
                val requestBody = RequestBody.create(MediaType.parse("image/jpeg"), file)
                if(imageAdapter.getItem(i) != "") {
                    info("file${i}")
                    val part = MultipartBody.Part.createFormData("file${i}", file.name, requestBody)
                    partList.add(part)
                }else partList.add(null)
                i++
            }while(i < 5)

            retrofitService.postRegisterShop(userIdx, shopName, shopTelNum,
                shopKeyword, shopInfo, shopAddress, shopLatitude, shopLongitude,
                partList[0], partList[1], partList[2], partList[3], partList[4])
                .enqueue(object : Callback<BaseResult>{
                    override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                        if(response.body()!!.baseModel.result == "Y") {
                            toast(getString(R.string.toast_request_approval_complete))
                            setResult(RESULT_OK)
                            finish()
                        }else toast(getString(R.string.toast_request_failed))
//                        info(response.body()!!.error.error)
                    }

                    override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                        error("요청 실패")
                        t.printStackTrace()
                    }
                })
        }
    }

    /**
     * 위도, 경도 구하기
     */
    private fun getGeoCode(): Pair<String?, String?> {
        val geocoder = Geocoder(this)
        val addresses = geocoder.getFromLocationName(addressEditText.text.toString(), 1)
        if (addresses != null && addresses.size > 0) {
            val address = addresses[0]
            return Pair(
                address.latitude.toString(), // 위도
                address.longitude.toString() // 경도
            )
        }

        return Pair(null, null)
    }

    /**
     * RequestBody 얻기
     */
    private fun getRequestBody(value: String) = RequestBody.create(MediaType.parse("text/plain"), value)

    /**
     * imageAdapter 설정
     */
    private fun setAdapter() {
        imageAdapter = ImageRecyclerAdapter().apply{
            addItem("")
            setOnItemClickListener(object : ImageRecyclerAdapter.OnItemClickListener{
                override fun onItemClick(holder: ImageRecyclerAdapter.ImageViewHolder, view: View, position: Int) {
                    when{
                        imageAdapter.itemCount == 5 -> toast(getString(R.string.toast_photo_limit))
                        position == 0 -> setPermission()
                    }
//                    view.deleteButton.setOnClickListener{v ->
//                        removeItem(position)
//                        notifyDataSetChanged()
//                    }
                }
            })
        }
        setImageRecyclerView()
        imageAdapter.notifyDataSetChanged()
    }

    /**
     * 저장소 권한 부여
     */
    private fun setPermission(){
        TedPermission.with(this@RegisterShopActivity)
            .setPermissionListener(object : PermissionListener{
                override fun onPermissionGranted() {
                    startActivityForResult(
                        Intent(Intent.ACTION_PICK).apply{ type = MediaStore.Images.Media.CONTENT_TYPE},
                        PICK_FROM_ALBUM_CODE
                    )
                }

                override fun onPermissionDenied(deniedPermissions: java.util.ArrayList<String>?) {
                    toast(getString(R.string.toast_ext_read_denied))
                }
            })
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }

    /**
     * 사진 리사이클러뷰 설정
     */
    private fun setImageRecyclerView() {
        imageRecyclerView.run{
            layoutManager = LinearLayoutManager(this@RegisterShopActivity, RecyclerView.HORIZONTAL, false)
            adapter = imageAdapter
            setOnTouchListener { _, event ->
                parent.requestDisallowInterceptTouchEvent(true)
                when(event.action and MotionEvent.ACTION_MASK){
                    MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
                }
                false
            }
        }
    }

    /**
     * 사진 선택하면 처리
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FROM_ALBUM_CODE && resultCode == RESULT_OK && data != null) {
            val uri = data.data!!
            info(uri)
            imageFile = File(getRealPathFromURI(uri))
            if (imageFile.isFile) {
                imageAdapter.run{
                    addItem(uri.toString())
                    notifyDataSetChanged()
                }
            }
        }
    }

    /**
     * 실제 경로 얻기
     */
    private fun getRealPathFromURI(uri: Uri): String{
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = baseContext.contentResolver.query(uri, proj, null, null, null)!!
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        }catch(e: Exception){
            return ""
        }finally{
            cursor?.let{ it.close() }
        }
    }
}
