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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
import kotlinx.android.synthetic.main.activity_register_shop.progressLayout
import kotlinx.android.synthetic.main.activity_register_shop.topTextView
import kotlinx.android.synthetic.main.item_image.view.*
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
const val BC_PICK_FROM_ALBUM_CODE = 201
const val REGISTER_SHOP_TAG = "RegisterShopActivity"
const val GENERAL_PIC_TYPE = 0
const val BC_PIC_TYPE = 1
class RegisterShopActivity : BaseActivity(), AnkoLogger, View.OnClickListener {
    override val loggerTag: String get() = REGISTER_SHOP_TAG
    private lateinit var imageFile: File
    private lateinit var imageAdapter: ImageRecyclerAdapter
    private lateinit var userIdx: String
    private var shopModel: ShopResult.ShopModel? = null
    private var isAppUpLoading = false
    private var bcPart: MultipartBody.Part? = null
    private var isBcPicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_shop)

        init()
        setViewListener()
    }

    private fun init(){
        setAdapter()
        // 업체 수정일 떄
        intent.getParcelableExtra<ShopResult.ShopModel>(SHOP_MODEL_KEY)?.let{
            shopModel = it
            setEditView(it)
            isBcPicked = true
        }
        // 업체 등록일 떄
        if(shopModel == null) {
            userIdx = intent.getStringExtra(FIELD_USER_IDX)!!
        }
//        setDescEditText()
    }

    private fun setEditView(shopModel: ShopResult.ShopModel) {
        Glide.with(this)
            .load(shopModel.shopBcImageUrl)
            .apply(RequestOptions().centerCrop())
            .into(addBcButton.imageView)
        topTextView.text = getString(R.string.edit_shop_info)
        shopNameEditText.setText(shopModel.shopName)
        telEditText.setText(shopModel.shopTelNum.replace("-", ""))
        addressEditText.setText(shopModel.shopAddress)
        // todo : 장비 종류 받아와서 미리 체크
        info(shopModel.shopItemKinds)
        GlobalScope.launch {
            shopModel.shopItemKinds.run {
                if (contains(getString(R.string.aerialPlatform))) runOnUiThread { checkBox1.isChecked = true }
                if (contains(getString(R.string.forkLift))) runOnUiThread { checkBox2.isChecked = true }
                if (contains(getString(R.string.towerCrane))) runOnUiThread { checkBox3.isChecked = true }
            }
        }
//        descEditText.setText(shopModel.shopInfo)
        textView17.visibility = View.GONE
        imageRecyclerView.visibility = View.GONE
    }

    private fun setViewListener() {
        requestButton.setOnClickListener(this)
        backButton.setOnClickListener(this)
        telEditText.addTextChangedListener(cpWatcher)
        addBcButton.setOnClickListener(this)
    }

    private val cpWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if(s.toString().length > 11) {
                telEditText.setText(s.toString().dropLast(1))
                telEditText.setSelection(11) // todo : 수정?
            }
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    override fun onClick(v: View?) {
        when(v){
            requestButton -> checkBlank()
            backButton -> onBackPressed()
            addBcButton -> setPermission(BC_PIC_TYPE)
//            mainImageView -> setPermission(MAIN_PICK_FROM_ALBUM_CODE)
        }
    }

    private fun checkBlank() {
        when{
            shopNameEditText.text.isEmpty() -> toast(getString(R.string.toast_type_shop_name))
            telEditText.text.isEmpty() or (telEditText.text.length < 10) -> toast(getString(R.string.toast_type_cp))
            addressEditText.text.isEmpty() -> toast(getString(R.string.toast_type_address))
            !checkBox1.isChecked && !checkBox2.isChecked && !checkBox3.isChecked -> toast(getString(R.string.toast_check_items))
//            descEditText.text.isEmpty() -> toast(getString(R.string.toast_type_info))
            else -> {
                hideKeyBoard()
                setAdapterEmptyListener()
                when(shopModel){
                    null -> checkImageBlank()
                    else -> requestEdit()
                }
            }
        }
    }

    override fun onBackPressed() {
        if(!isAppUpLoading) super.onBackPressed()
    }

    /**
     * ImageRecyclerAdapter 클릭 이벤트 없애는 메소드
     */
    private fun setAdapterEmptyListener(){
        imageAdapter.setOnItemClickListener(object : ImageRecyclerAdapter.OnItemClickListener{
            override fun onItemClick(holder: ImageRecyclerAdapter.ImageViewHolder, view: View, position: Int) {}
        })
    }

    /**
     * 사진 선택했는지 체크
     */
    private fun checkImageBlank(){
        when{
            imageAdapter.getRealSize() == 0 -> toast(getString(R.string.toast_select_image))
            !isBcPicked -> toast(getString(R.string.toast_select_bcImage))
            else -> requestApproval()
        }
    }

    private fun hideKeyBoard() {
        imm.hideSoftInputFromWindow(rootLayout.windowToken, 0)
    }

    /**
     * 업체 정보 수정
     */
    private fun requestEdit() {
        setViewWhenUploading()
        val shopIdx = getRequestBody(shopModel!!.shopIdx)
        val shopName = getRequestBody(shopNameEditText.text.toString())
        val shopTelNum = getRequestBody(telEditText.text.toString())
        val shopItemKinds = getRequestBody(getItemKinds())
//        val shopInfo = descEditText.text.toString()
        val shopAddress = getRequestBody(addressEditText.text.toString())
        val shopLatitude = getRequestBody(getGeoCode().first.toString())
        val shopLongitude = getRequestBody(getGeoCode().second.toString())

        progressLayout.visibility = View.VISIBLE
        retrofitService.postEditShop(shopIdx, shopName, shopTelNum, shopItemKinds, shopAddress, shopLatitude, shopLongitude, bcPart, shopItemKinds)
            .enqueue(object : Callback<BaseResult>{
                override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                    if(response.body()!!.baseModel.result == "Y"){
                        toast(getString(R.string.toast_request_edit_complete))
                        setResult(RESULT_OK, Intent().apply{
                            putExtra(FIELD_SHOP_NAME, shopNameEditText.text.toString())
                        })
                        finish()
                    }else toast(getString(R.string.toast_request_failed))
                }

                override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                    error("요청 실패")
                    t.printStackTrace()
                }
            })
    }

    private fun setViewWhenUploading(){
        isAppUpLoading = true
        shopNameEditText.isEnabled = false
        telEditText.isEnabled = false
        addressEditText.isEnabled = false
        checkBox1.isEnabled = false
        checkBox2.isEnabled = false
        checkBox3.isEnabled = false
//        descEditText.isEnabled = false
        imageRecyclerView.isEnabled = false
        requestButton.isEnabled = false
    }

    /**
     * 업체 등록 요청
     */
    private fun requestApproval() {
        setViewWhenUploading()
        info("requestApproval실행됨")
        val userIdx = getRequestBody(userIdx)
        val shopName = getRequestBody(shopNameEditText.text.toString())
        val shopTelNum = getRequestBody(telEditText.text.toString())
        val shopItemKinds = getRequestBody(getItemKinds())
//        val shopInfo = getRequestBody(descEditText.text.toString())
        val shopAddress = getRequestBody(addressEditText.text.toString())
        val shopLatitude = getRequestBody(getGeoCode().first ?: "0")
        val shopLongitude = getRequestBody(getGeoCode().second ?: "0")
        val partList = ArrayList<MultipartBody.Part?>(5)
        info(this.userIdx)
        info(shopNameEditText.text.toString())
        info(telEditText.text.toString())
        info(shopItemKinds)
        info(addressEditText.text.toString())
        progressLayout.visibility = View.VISIBLE
        // 사진 파일 처리, 업체 등록 요청
        GlobalScope.launch {
            info("Coroutine 실행됨")
            while(imageAdapter.itemCount < 5){
                imageAdapter.addItem("")
            }
            var i = 0
            do{
                if(imageAdapter.getItem(i) != "") {
                    info("uri " + imageAdapter.getItem(i))
                    val realPath = getRealPathFromURI(Uri.parse(imageAdapter.getItem(i)))
//                    info("실제경로 " + realPath)
                    val file = getResizedFile(this@RegisterShopActivity, realPath, MIN_IMAGE_SIZE)
                    val requestBody = RequestBody.create(MediaType.parse(""), file)
                    info("file${i + 1}")
                    val part = MultipartBody.Part.createFormData("file${i + 1}", file.name, requestBody)
                    partList.add(part)
                }else {
                    info("uri is \"\" " + imageAdapter.getItem(i))
                    partList.add(null)
                }
                i++
            }while(i < 5)


            retrofitService.postRegisterShop(userIdx, shopName, shopTelNum,
                shopItemKinds, shopAddress, shopLatitude, shopLongitude,
                partList[0], partList[1], partList[2], partList[3], partList[4], bcPart)
                .enqueue(object : Callback<BaseResult>{
                    override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                        if(response.body()!!.baseModel.result == "Y") {
                            info(response.body()!!.baseModel)
                            toast(getString(R.string.toast_request_approval_complete))
                            setResult(RESULT_OK)
                            finish()
                        }else toast(getString(R.string.toast_request_failed))
//                        info(response.body()!!.error.error)
                    }

                    override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                        error("요청 실패")
                        finish()
                        t.printStackTrace()
                    }
                })
        }
    }

    private fun getItemKinds(): String{
        var s = ""
        if(checkBox1.isChecked) s += "${checkBox1.text},"
        if(checkBox2.isChecked) s += "${checkBox2.text},"
        if(checkBox3.isChecked) s += "${checkBox3.text}"
        return if(s.endsWith(',')) s.dropLast(1) else s
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
     * imageAdapter 설정
     */
    private fun setAdapter() {
        imageAdapter = ImageRecyclerAdapter().apply{
            addItem("")
            setOnItemClickListener(object : ImageRecyclerAdapter.OnItemClickListener{
                override fun onItemClick(holder: ImageRecyclerAdapter.ImageViewHolder, view: View, position: Int) {
                    when{
                        imageAdapter.getRealSize() == 5 -> toast(getString(R.string.toast_photo_limit))
                        position == 0 -> setPermission(GENERAL_PIC_TYPE)
                    }
                }
            })
        }
        setImageRecyclerView()
        imageAdapter.notifyDataSetChanged()
    }

    /**
     * 저장소 권한 부여
     */
    private fun setPermission(picType: Int){
        TedPermission.with(this@RegisterShopActivity)
            .setPermissionListener(object : PermissionListener{
                override fun onPermissionGranted() {
                    when(picType) {
                        GENERAL_PIC_TYPE -> pickFromAlbum()
                        BC_PIC_TYPE -> pickBcFromAlbum()
                    }
                }

                override fun onPermissionDenied(deniedPermissions: java.util.ArrayList<String>?) {
                    toast(getString(R.string.toast_ext_read_denied))
                }
            })
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check()
    }

    private fun pickBcFromAlbum() {
        startActivityForResult(
            Intent(Intent.ACTION_PICK).apply{
                type = MediaStore.Images.Media.CONTENT_TYPE
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }, BC_PICK_FROM_ALBUM_CODE)
    }

    private fun pickFromAlbum(){
        startActivityForResult(
            Intent(Intent.ACTION_PICK).apply {
                type = MediaStore.Images.Media.CONTENT_TYPE
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }, PICK_FROM_ALBUM_CODE
        )
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
            GlobalScope.launch {
                val uri = data.data!!
                info(uri)
                imageFile = File(getRealPathFromURI(uri))
                if (imageFile.isFile) {
                    imageAdapter.run {
                        addItem(uri.toString())
                        runOnUiThread{ notifyDataSetChanged() }
                    }
                }
            }
        }
        if(requestCode == BC_PICK_FROM_ALBUM_CODE && resultCode == RESULT_OK && data != null){
            GlobalScope.launch {
                val uri = data.data!!
                info("받은 URI : $uri")
                val bcFile = getResizedFile(this@RegisterShopActivity, getRealPathFromURI(uri), MIN_IMAGE_SIZE)
                if (bcFile.isFile){
                    isBcPicked = true
                    info("파일 크기 = ${bcFile.length()}")
                    val requestBody = RequestBody.create(MediaType.parse(""), bcFile)
                    bcPart = MultipartBody.Part.createFormData(FIELD_SHOP_BC_PIC, bcFile.name, requestBody)
                    runOnUiThread {
                        Glide.with(this@RegisterShopActivity)
                            .load(uri)
                            .apply(RequestOptions().centerCrop())
                            .into(addBcButton.imageView)
                    }
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
            cursor = applicationContext.contentResolver.query(uri, proj, null, null, null)!!
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        }catch(e: Exception){
            return ""
        }finally{
            cursor?.let{ it.close() }
        }
    }

//    private fun setDescEditText() {
//        descEditText.setOnTouchListener{ view, event ->
//            if (view == descEditText) {
//                view.parent.requestDisallowInterceptTouchEvent(true)
//                when (event.action and MotionEvent.ACTION_MASK) {
//                    MotionEvent.ACTION_UP -> view.parent.requestDisallowInterceptTouchEvent(false)
//                }
//            }
//            false
//        }
//    }
}
