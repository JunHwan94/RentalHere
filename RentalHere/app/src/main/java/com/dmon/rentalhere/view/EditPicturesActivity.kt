package com.dmon.rentalhere.view

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.dmon.rentalhere.BaseActivity
import com.dmon.rentalhere.R
import com.dmon.rentalhere.adapter.EditImageRecyclerAdapter
import com.dmon.rentalhere.adapter.PICK_FROM_ALBUM_AND_OVERRIDE_CODE
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.model.ShopResult
import com.dmon.rentalhere.retrofit.FIELD_SHOP_IDX
import com.dmon.rentalhere.retrofit.FIELD_SHOP_MAIN_PIC_NUM
import kotlinx.android.synthetic.main.activity_edit_pictures.*
import kotlinx.android.synthetic.main.fragment_image.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
const val EDIT_PIC_TAG = "EditPicturesActivity"
class EditPicturesActivity : BaseActivity(), View.OnClickListener, AnkoLogger {
    override val loggerTag: String get() = EDIT_PIC_TAG
    private lateinit var shopModel: ShopResult.ShopModel
    private lateinit var adapter: EditImageRecyclerAdapter
    private var isChanged = false
    private var isMainSelected = true
    private var mainPosition = 0
    private var positionToEdit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pictures)
        toast(getString(R.string.toast_main_has_border)).apply{ duration = Toast.LENGTH_LONG }
        processIntent()
        setViewListener()
    }

    private fun processIntent(){
        shopModel = intent.getParcelableExtra(SHOP_MODEL_KEY)!!
        adapter = EditImageRecyclerAdapter(this@EditPicturesActivity, shopModel.shopIdx, shopModel.mainPicNum).apply {
            setOnItemClickListener(object : EditImageRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(holder: EditImageRecyclerAdapter.ImageViewHolder, view: View, position: Int) {
                    holder.itemView.rootLayout.run{
                        when(isMainSelected){
                            true ->
                                when(background){
                                    null -> toast(getString(R.string.toast_main_already_selected))
                                    else -> {
                                        background = null
                                        isMainSelected = false
                                    }
                                }
                            false -> {
                                background = getDrawable(R.drawable.fill_primary)
                                isMainSelected = true
                                mainPosition = position
                            }
                        }
                    }
                }
            })
        }
        setAdapter(adapter)
    }

    private fun setAdapter(adapter: EditImageRecyclerAdapter) {
        GlobalScope.launch{
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
        }
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
            completeButton -> postEditMainPicture()
        }
    }

    private fun pickFromAlbum(){
        if(adapter.itemCount == 5) {
            toast(getString(R.string.toast_photo_limit))
        }else{
            startActivityForResult(Intent(Intent.ACTION_PICK).apply {
                type = MediaStore.Images.Media.CONTENT_TYPE
            }, PICK_FROM_ALBUM_CODE)
        }
    }

    private fun postEditMainPicture() {
        val map = HashMap<String, Any>().apply{
            this[FIELD_SHOP_IDX] = shopModel.shopIdx
            this[FIELD_SHOP_MAIN_PIC_NUM] = mainPosition + 1
        }
        retrofitService.postEditShopMainPicture(map).enqueue(object : Callback<BaseResult>{
            override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                if(response.body()!!.baseModel.result == "Y"){
                    toast(getString(R.string.toast_main_pic_changed))
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }

            override fun onFailure(call: Call<BaseResult>, t: Throwable) {

            }
        })
    }

    override fun onBackPressed() {
        if(isChanged) setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_FROM_ALBUM_CODE && resultCode == RESULT_OK && data != null){
            val file = File(getRealPathFromURI(data.data!!))
            val requestBody = RequestBody.create(MediaType.parse(""), file)
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
            retrofitService.postEditShopPicture(getRequestBody(shopModel.shopIdx), getRequestBody((adapter.itemCount+1).toString()), part)
                .enqueue(object : Callback<BaseResult>{
                    override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                        info(response.body()!!.baseModel.toString())
                        if(response.body()!!.baseModel.result == "Y"){
                            adapter.run{
                                addItem(data.data.toString())
                                notifyDataSetChanged()
                                isChanged = true
                            }
                        }
                    }

                    override fun onFailure(call: Call<BaseResult>, t: Throwable) {

                    }
                })
//            addOrReplacePicture(requestCode, data, adapter.itemCount+1)
        }
        if(requestCode == PICK_FROM_ALBUM_AND_OVERRIDE_CODE && resultCode == Activity.RESULT_OK && data != null){
            val file = File(getRealPathFromURI(data.data!!))
            val requestBody = RequestBody.create(MediaType.parse(""), file)
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
            retrofitService.postEditShopPicture(getRequestBody(shopModel.shopIdx), getRequestBody((positionToEdit+1).toString()), part)
                .enqueue(object : Callback<BaseResult>{
                    override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                        info(response.body()!!.baseModel.toString())
                        if(response.body()!!.baseModel.result == "Y"){
                            adapter.run{
                                replaceItem(positionToEdit, data.data.toString())
                                notifyDataSetChanged()
                                isChanged = true
                            }
                        }
                    }

                    override fun onFailure(call: Call<BaseResult>, t: Throwable) {

                    }
                })
//            addOrReplacePicture(requestCode, data, positionToEdit)
        }
    }

    fun addOrReplacePicture(requestCode: Int, data: Intent, position: Int){
        val file = File(getRealPathFromURI(data.data!!))
        val requestBody = RequestBody.create(MediaType.parse(""), file)
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        retrofitService.postEditShopPicture(getRequestBody(shopModel.shopIdx), getRequestBody((position+1).toString()), part)
            .enqueue(object : Callback<BaseResult>{
                override fun onResponse(call: Call<BaseResult>, response: Response<BaseResult>) {
                    info(response.body()!!.baseModel.toString())
                    if(response.body()!!.baseModel.result == "Y"){
                        adapter.run{
                            when(requestCode){
                                PICK_FROM_ALBUM_CODE -> addItem(data.data.toString())
                                PICK_FROM_ALBUM_AND_OVERRIDE_CODE -> replaceItem(position, data.data.toString())
                            }
                            notifyDataSetChanged()
                            isChanged = true
                        }
                    }
                }

                override fun onFailure(call: Call<BaseResult>, t: Throwable) {

                }
            })
    }

    fun getRequestBody(value: String) = RequestBody.create(MediaType.parse("text/plain"), value)

    fun editPicture(position: Int){
        positionToEdit = position
        startActivityForResult(Intent(Intent.ACTION_PICK).apply{
            type = MediaStore.Images.Media.CONTENT_TYPE
        }, PICK_FROM_ALBUM_AND_OVERRIDE_CODE)
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
