package com.dmon.rentalhere.adapter

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dmon.rentalhere.R
import com.dmon.rentalhere.databinding.FragmentImageBinding
import com.dmon.rentalhere.model.BaseResult
import com.dmon.rentalhere.retrofit.*
import com.dmon.rentalhere.view.EditPicturesActivity
import kotlinx.android.synthetic.main.fragment_image.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val PICK_FROM_ALBUM_AND_OVERRIDE_CODE = 303
class EditImageRecyclerAdapter(private val activity: Activity, private val shopIdx: String, private val mainPosition: String): RecyclerView.Adapter<EditImageRecyclerAdapter.ImageViewHolder>() {
    private val uriList = ArrayList<String>()
    private lateinit var listener: OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(holder: ImageViewHolder, view: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_image, parent, false)
        return ImageViewHolder(view, this, activity, shopIdx, mainPosition)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = uriList[position]
        holder.setItem(uri)
        holder.setOnItemClickListener(listener)
    }

    override fun getItemCount(): Int = uriList.size
    fun addItem(uri: String){
        if(uri != "") uriList.add(uri)
//        if(uriList.size == 6) uriList.removeAt(0)
    }
    fun removeItem(position: Int) = uriList.removeAt(position)
    fun replaceItem(position: Int, uri: String){ uriList[position] = uri }
    fun getItem(position: Int) = uriList[position]
    fun getItems() = uriList
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    class ImageViewHolder(itemView: View, val adapter: EditImageRecyclerAdapter, private var activity: Activity, private val shopIdx: String, private val mainPosition: String) : RecyclerView.ViewHolder(itemView){
        private lateinit var listener: OnItemClickListener
        private var binding = FragmentImageBinding.bind(itemView)
        private var retrofitService: RetrofitService = RetrofitClient.getRetrofitInstance()!!.create(RetrofitService::class.java)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if(listener != null) listener.onItemClick(this, itemView, position)
            }
            itemView.editButton.setOnClickListener{ editPicture() }
            itemView.deleteButton.setOnClickListener{ deletePicture() }
        }

        fun setItem(uri: String){
            if(uri != ""){
                Glide.with(itemView.context)
                    .load(Uri.parse(uri))
                    .apply(RequestOptions().centerCrop())
                    .into(binding.imageView)
                binding.editButton.visibility = View.VISIBLE
                binding.deleteButton.visibility = View.VISIBLE
                Log.e(mainPosition, position.toString())
                if(mainPosition.toInt() == position + 1) binding.rootLayout.background = activity.getDrawable(R.drawable.fill_primary)
            }
        }

        fun setOnItemClickListener(listener: OnItemClickListener){
            this.listener = listener
        }

        fun editPicture(){
            (activity as EditPicturesActivity).editPicture(position)
        }

        fun deletePicture(){
            activity.also{
                when {
                    adapter.itemCount == 1 -> it.toast(it.getString(R.string.toast_must_have_picture))
                    binding.rootLayout.background != null -> it.toast(it.getString(R.string.toast_cannot_remove_main))
                    else -> it.alert(it.getString(R.string.dialog_delete_picture)) {
                        positiveButton(it.getString(R.string.confirm)) {
                            Log.d("포지션", position.inc().toString())
                            requestDelete()
                        }
                        negativeButton(it.getString(R.string.cancel)) {}
                    }.show()
                }
            }
        }

        fun requestDelete(){
            val map = HashMap<String, Any>().apply {
                this[FIELD_SHOP_IDX] = shopIdx
                this[FIELD_SHOP_PIC_NUM] = position.inc().toString()
            }
            retrofitService.postDeleteShopPicture(map)
                .enqueue(object : Callback<BaseResult> {
                    override fun onResponse(
                        call: Call<BaseResult>,
                        response: Response<BaseResult>
                    ) {
                        adapter.run {
                            removeItem(position)
                            notifyDataSetChanged()
                        }
                    }

                    override fun onFailure(call: Call<BaseResult>, t: Throwable) {

                    }
                })
        }
    }
}