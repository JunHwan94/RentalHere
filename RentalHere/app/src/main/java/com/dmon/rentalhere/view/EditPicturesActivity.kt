package com.dmon.rentalhere.view

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.dmon.rentalhere.BaseActivity
import com.dmon.rentalhere.R
import com.dmon.rentalhere.adapter.EditImageRecyclerAdapter
import com.dmon.rentalhere.adapter.PICK_FROM_ALBUM_AND_OVERRIDE_CODE
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.model.ShopResult
import com.dmon.rentalhere.retrofit.FIELD_SHOP_IDX
import com.dmon.rentalhere.retrofit.FIELD_SHOP_MAIN_PIC_NUM
import com.dmon.rentalhere.retrofit.FIELD_SHOP_PIC_NUM
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_edit_pictures.*
import kotlinx.android.synthetic.main.fragment_image.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

const val EDIT_PIC_TAG = "EditPicturesActivity"
const val MIN_IMAGE_SIZE = 1000
class EditPicturesActivity : BaseActivity(), View.OnClickListener, AnkoLogger {
    override val loggerTag: String get() = EDIT_PIC_TAG
    private lateinit var shopModel: ShopResult.ShopModel
    private lateinit var adapter: EditImageRecyclerAdapter
    private var isChanged = false
    private var isMainSelected = true // todo 테스트
    private var mainPosition = 0
    private var positionToEdit = 0
    private lateinit var uploadFileMap: HashMap<Int, File>
    private lateinit var deleteList: ArrayList<Int>
    private var uploadCnt: Int = 0
    private var isAppUpLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pictures)
//        toast(getString(R.string.toast_main_has_border)).apply{ duration = Toast.LENGTH_LONG }
        processIntent()
        setViewListener()
        Snackbar.make(completeButton, getString(R.string.toast_main_has_border) + "\n현재 메인 사진 : ${mainPosition + 1}번째", Snackbar.LENGTH_INDEFINITE).run{
            setAction(getString(R.string.confirm)){ dismiss() }
            show()
        }
    }

    private fun processIntent(){
        uploadFileMap = HashMap()
        deleteList = ArrayList()
        shopModel = intent.getParcelableExtra(SHOP_MODEL_KEY)!!
        mainPosition = shopModel.mainPicNum.toInt()
        adapter = EditImageRecyclerAdapter(this@EditPicturesActivity, shopModel.shopIdx, mainPosition)
        adapter.run{
            setOnItemClickListener(object : EditImageRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(holder: EditImageRecyclerAdapter.ImageViewHolder, view: View, position: Int) {
                    processItemView(holder, position)
                }
            })
        }
        setAdapter(adapter)
    }

    private fun processItemView(holder: EditImageRecyclerAdapter.ImageViewHolder, position: Int){
        holder.itemView.rootLayout.run {
            when (isMainSelected) {
                true ->
                    when (background) {
                        null -> toast(getString(R.string.toast_main_already_selected))
                        else -> {
                            background = null
                            isMainSelected = false
                        }
                    }
                false -> {
                    background = getDrawable(R.drawable.fill_primary)
                    isMainSelected = true
                    this@EditPicturesActivity.mainPosition = position
                    info("선택됨 : $position")
                    setAdapterMainPosition(holder, position)
                }
            }
        }
    }

    fun setAdapterMainPosition(holder: EditImageRecyclerAdapter.ImageViewHolder, position: Int){
        adapter.run{
            mainPosition = position
            onBindViewHolder(holder, position)
        }
    }

    private fun setAdapter(adapter: EditImageRecyclerAdapter) {
//        GlobalScope.launch{
            runBlocking {
                recyclerView.run {
                    this.adapter = adapter
                    layoutManager = LinearLayoutManager(this@EditPicturesActivity)
                }
            }
            adapter.addItem(shopModel.shopProfileImageUrl1)
            adapter.addItem(shopModel.shopProfileImageUrl2)
            adapter.addItem(shopModel.shopProfileImageUrl3)
            adapter.addItem(shopModel.shopProfileImageUrl4)
            adapter.addItem(shopModel.shopProfileImageUrl5)
            runOnUiThread { adapter.notifyDataSetChanged() }
//        }
    }

    private fun setViewListener() {
        backButton.setOnClickListener(this)
        addButton.setOnClickListener(this)
        completeButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            backButton -> onBackPressed()
            addButton -> pickFromAlbum()
            completeButton -> deleteFiles()
        }
    }

    /**
     * DB의 파일을 하나씩 삭제 요청하는 메소드
     */
    private fun deleteFiles() {
        setViewWhenUploading()
        GlobalScope.launch {
            when{
                deleteList.size != 0 -> sequence { yieldAll(deleteList) }
                    .forEach {
                        val map = HashMap<String, Any>().apply {
                            this[FIELD_SHOP_IDX] = shopModel.shopIdx
                            this[FIELD_SHOP_PIC_NUM] = (it + 1).toString()
                        }
                        retrofitService.postDeleteShopPicture(map)
                            .enqueue(object : Callback<BaseResult> {
                                override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                                    postEditMainPicture()
                                }

                                override fun onFailure(call: Call<BaseResult>, t: Throwable) {

                                }
                            })
                    }
                deleteList.size == 0 -> postEditMainPicture()
            }
        }
    }

    /**
     * 업로드중 뷰를 못쓰게하는 메소드
     */
    private fun setViewWhenUploading(){
        isAppUpLoading = true
        progressLayout.visibility = View.VISIBLE
        backButton.isEnabled = false
        recyclerView.isEnabled = false
        addButton.isEnabled = false
        completeButton.isEnabled = false
    }

    /**
     * 사진 불러오기
     */
    private fun pickFromAlbum(){
        if(adapter.itemCount == 5) {
            toast(getString(R.string.toast_photo_limit))
        }else{
            startActivityForResult(Intent(Intent.ACTION_PICK).apply {
                type = MediaStore.Images.Media.CONTENT_TYPE
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }, PICK_FROM_ALBUM_CODE)
        }
    }

    /**
     * 메인 사진으로 쓸 사진을 수정 요청하는 메소드
     */
    private fun postEditMainPicture() {
        //todo : 테스트
        try{ adapter.getItem(mainPosition) }
        catch(e: Exception) { mainPosition-- }
//        info("메인 사진 변경 요청")
        val map = HashMap<String, Any>().apply{
            this[FIELD_SHOP_IDX] = shopModel.shopIdx
            this[FIELD_SHOP_MAIN_PIC_NUM] = mainPosition
        }
        info("메인 사진은 ${mainPosition}번째")
        retrofitService.postEditShopMainPicture(map).enqueue(object : Callback<BaseResult>{
            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                if(response.body()!!.baseModel.result == "Y"){
//                    toast(getString(R.string.toast_main_pic_changed))
                    uploadFiles()
//                    info("메인 사진 변경 완료")
                }
            }

            override fun onFailure(call: Call<BaseResult>, t: Throwable) {

            }
        })
    }

    /**
     * 수정또는 추가된 파일 존재여부에 따라 처리하는 메소드
     */
    private fun uploadFiles(){
        GlobalScope.launch {
            when {
                uploadFileMap.size != 0 -> {
//                    info("업로드 갯수 ${uploadFileMap.size}")
                    runUpload()
                }
                uploadFileMap.size == 0 -> {
//                    info("업로드 갯수 0")
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

    /**
     * 사진 파일 업로드하는 메소드
     */
    private fun runUpload(){
        uploadFileMap.run {
            keys.forEach {
//                info(it)
                val file = uploadFileMap[it]!!
                val requestBody = RequestBody.create(MediaType.parse(""), file)
                val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
                retrofitService.postEditShopPicture(getRequestBody(shopModel.shopIdx), getRequestBody(it.toString()), part)
                    .enqueue(object : Callback<BaseResult> {
                        override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                            info(response.body()!!.baseModel.toString())
                            if (response.body()!!.baseModel.result == "Y") {
                                uploadCnt++
                                if(uploadCnt == uploadFileMap.size){
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }
                        }

                        override fun onFailure(call: Call<BaseResult>, t: Throwable) {
                            error("요청 실패")
                        }
                    })
            }
        }
    }

    override fun onBackPressed() {
        if(!isAppUpLoading) {
            if (isChanged) setResult(Activity.RESULT_OK)
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_FROM_ALBUM_CODE && resultCode == RESULT_OK && data != null){
            info("메인 사진 : $mainPosition 번째")
            GlobalScope.launch {
                val file = getResizedFile(this@EditPicturesActivity, getRealPathFromURI(data.data!!), MIN_IMAGE_SIZE)
//            info("파일 크기 = ${file.length()}")
                uploadFileMap[adapter.itemCount + 1] = file
                adapter.run {
                    addItem(data.data.toString())
                    runOnUiThread { notifyDataSetChanged() }
                }
            }
        }
        if(requestCode == PICK_FROM_ALBUM_AND_OVERRIDE_CODE && resultCode == Activity.RESULT_OK && data != null){
            info("메인 사진 : $mainPosition 번째")
            GlobalScope.launch {
                val file = getResizedFile(this@EditPicturesActivity, getRealPathFromURI(data.data!!), MIN_IMAGE_SIZE)
                uploadFileMap[positionToEdit + 1] = file
                adapter.run {
                    replaceItem(positionToEdit, data.data.toString())
                    runOnUiThread { notifyDataSetChanged() }
                }
            }
        }
    }

    fun editPicture(position: Int){
        positionToEdit = position
        startActivityForResult(Intent(Intent.ACTION_PICK).apply{
            type = MediaStore.Images.Media.CONTENT_TYPE
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }, PICK_FROM_ALBUM_AND_OVERRIDE_CODE)
    }

    /**
     * DB에서 삭제할 위치 설정
     * 업로드할 사진 파일을 터치하여 삭제한 경우 어댑터에서 삭제
     * 업로드할 사진 목록 uploadFileMap에서 삭제
     * @param position 삭제할 사진 위치
     */
    fun deletePicture(position: Int, holder: EditImageRecyclerAdapter.ImageViewHolder){
        if(position < mainPosition) {
            adapter.onBindViewHolder(holder, mainPosition)
            mainPosition--
            adapter.mainPosition--
        }
        deleteList.add(position)
        adapter.run{
            removeItem(position)
            notifyDataSetChanged()
        }
        uploadFileMap.run{
            remove(position + 1)
            keys.filter{
                position + 1 < it
            }.forEach{
                this[it - 1] = this[it]!!
            }
        }

//        generateSequence(0){ it + 1 }.take(adapter.itemCount)
//            .filter{ it == mainPosition }
//            .forEach{
//                info("인덱스 $it 변경됨")
//                adapter.run{
//                    onBindViewHolder(holder, it)
//                }
//            }
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

//    fun addOrReplacePicture(requestCode: Int, data: Intent, position: Int){
//        val file = File(getRealPathFromURI(data.data!!))
//        val requestBody = RequestBody.create(MediaType.parse(""), file)
//        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
//        retrofitService.postEditShopPicture(getRequestBody(shopModel.shopIdx), getRequestBody((position+1).toString()), part)
//            .enqueue(object : Callback<BaseResult>{
//                override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
//                    info(response.body()!!.baseModel.toString())
//                    if(response.body()!!.baseModel.result == "Y"){
//                        adapter.run{
//                            when(requestCode){
//                                PICK_FROM_ALBUM_CODE -> addItem(data.data.toString())
//                                PICK_FROM_ALBUM_AND_OVERRIDE_CODE -> replaceItem(position, data.data.toString())
//                            }
//                            notifyDataSetChanged()
//                            isChanged = true
//                        }
//                    }
//                }
//
//                override fun onFailure(call: Call<BaseResult>, t: Throwable) {
//
//                }
//            })
//    }
}
