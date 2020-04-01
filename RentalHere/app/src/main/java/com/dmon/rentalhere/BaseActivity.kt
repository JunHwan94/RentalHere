package com.dmon.rentalhere

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.dmon.rentalhere.model.UserInfoResult
import com.dmon.rentalhere.presenter.CLIENT_TYPE
import com.dmon.rentalhere.presenter.PREF_KEY
import com.dmon.rentalhere.presenter.TYPE_KEY
import com.dmon.rentalhere.retrofit.RetrofitClient
import com.dmon.rentalhere.retrofit.RetrofitService
import com.dmon.rentalhere.view.*
import kotlinx.android.synthetic.main.activity_client_main.*
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.*
import java.util.*
const val MAX_IMAGE_SIZE = 768 * 768
abstract class BaseActivity : AppCompatActivity(){
    lateinit var imm: InputMethodManager
    lateinit var retrofitService: RetrofitService
    var userModel: UserInfoResult.UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == EDIT_USER_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                it.getParcelableExtra<UserInfoResult.UserModel>(USER_MODEL_KEY)?.let { userModel ->
                    this.userModel = userModel
                }
            }
        }
    }

    /**
     *  내비게이션 뷰 열기 / 닫기
     */
    fun moveDrawer() {
        if(!drawer.isDrawerOpen(GravityCompat.END))
            drawer.openDrawer(GravityCompat.END)
        else drawer.closeDrawer(GravityCompat.END)
    }

    /**
     * 정보 수정 (SignUpActivity 실행)
     * @param userModel: 기존 정보 설정을 위해 넘겨줌
     */
    fun editUser(userModel: UserInfoResult.UserModel, userType: Int) {
        startActivityForResult(Intent(this, SignUpActivity::class.java).apply{
            putExtra(USER_MODEL_KEY, userModel)
            putExtra(TYPE_KEY, userType)
        }, EDIT_USER_CODE)
    }

    /**
     * 로그아웃
     */
    fun logOut(){
        val editor = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE).edit()
        with(editor){
            clear()
            commit()
        }
//        startActivity(Intent(this, ClientMainActivity::class.java).apply{
//            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or
//                    Intent.FLAG_ACTIVITY_NEW_TASK
//        })
//        startActivity(Intent(this, LoginActivity::class.java).apply{
//            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            putExtra(TYPE_KEY, CLIENT_TYPE)
//        })
//        finish()
    }

    /**
     * 요청 시 보낼 필드에 따른 값을 넣으면
     * RequestBody 객체를 반환함
     */
    val getRequestBody: (String) -> RequestBody = { RequestBody.create(MediaType.parse("text/plain"), it) }

    /**
     * 사진 크기 줄이기
     * @param context: Context
     * @param uri: Uri
     * @param resize: 반환하는 파일 최소 크기
     * @return 처리한 비트맵으로 캐시에 저장된 파일 반환
     */
    fun getResizedFile(context: Context, uri: String, resize: Int): File {
        val options = BitmapFactory.Options()
        /************************************ 추가한 부분 *************************/
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(uri, options)
        options.inSampleSize = 4
        /***********************************************************************/

        var width = options.outWidth
        var height = options.outHeight
        var sampleSize = 2

        while (true) {//2번
            if (width / 2 < resize || height / 2 < resize)
                break
            width /= 2
            height /= 2
            sampleSize *= 2
        }

        options.inSampleSize = sampleSize

        /********************************************* 추가 *********************************************/
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = getCompressedBitmap(uri, options)
        /************************************************************************************************/

        return getCacheImageFile(context, bitmap)
    }

    /**
     * 압축한 비트맵을 반환하는 메소드
     * @param uri 사진의 uri
     */
    private fun getCompressedBitmap(uri: String, options: BitmapFactory.Options): Bitmap{
        var bitmap = BitmapFactory.decodeFile(uri, options)
        try{
            if(bitmap != null){
                bitmap = rotateBitmap(uri, bitmap)
            }
        }catch(e: Exception){
            e.printStackTrace()
        }

        var quality = 100
        var streamLength: Int
        do{
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bmpStream)
            val bitmapByteArray = bmpStream.toByteArray()
            streamLength = bitmapByteArray.size
            quality -= 5
        }
        while(streamLength >= MAX_IMAGE_SIZE)

        return bitmap
    }

    /**
     * 원래 방향에 맞춰 회전
     */
    private fun rotateBitmap(uri: String, bitmap: Bitmap): Bitmap{
        val exifInterface = ExifInterface(uri)
        val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        return when(orientation){
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }
    }
    /**
     * 이미지를 회전시킵니다.
     *
     * @param source 비트맵 이미지
     * @param angle 회전 각도
     * @return 회전된 이미지
     */
    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * 캐시파일 생성해서 업로드 준비
     */
    private fun getCacheImageFile(context: Context, bitmap: Bitmap): File {
        val storage = context.cacheDir
        val realTime = Calendar.getInstance().timeInMillis
        val ext = ".jpg"
        val fullFileName = realTime.toString() + ext
        val tempFile = File(storage, fullFileName)

        var fos: FileOutputStream? = null
        try {
            tempFile.createNewFile()
            fos = FileOutputStream(tempFile)
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, fos)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fos?.let { it.close() }
            return tempFile
        }
    }
}