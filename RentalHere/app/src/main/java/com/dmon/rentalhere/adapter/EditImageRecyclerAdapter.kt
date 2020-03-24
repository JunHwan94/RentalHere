package com.dmon.rentalhere.adapter

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dmon.rentalhere.R
import com.dmon.rentalhere.databinding.FragmentImageBinding
import com.dmon.rentalhere.view.EditPicturesActivity
import kotlinx.android.synthetic.main.fragment_image.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.margin
import org.jetbrains.anko.toast

const val PICK_FROM_ALBUM_AND_OVERWRITE_CODE = 303
class EditImageRecyclerAdapter(private val activity: Activity, private val shopIdx: String, var mainPosition: Int): RecyclerView.Adapter<EditImageRecyclerAdapter.ImageViewHolder>() {
    private val uriList = ArrayList<String>()
    private lateinit var listener: OnItemClickListener

    fun interface OnItemClickListener{
        fun onItemClick(holder: ImageViewHolder, view: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_image, parent, false)
//        Log.e("메인 위치", "$mainPosition")
        return ImageViewHolder(view, this, activity, shopIdx, mainPosition)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = uriList[position]
        holder.mainPosition = mainPosition
        Log.e("메인 위치", "$mainPosition")
        holder.setItem(uri)
        holder.setOnItemClickListener(listener)
    }

    override fun getItemCount(): Int = uriList.size
    fun addItem(uri: String){
        if(uri != "") uriList.add(uri)
    }
    fun removeItem(position: Int) = uriList.removeAt(position)
    fun replaceItem(position: Int, uri: String){ uriList[position] = uri }
    fun getItem(position: Int) = uriList[position]
    fun getItems() = uriList
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    class ImageViewHolder(itemView: View, val adapter: EditImageRecyclerAdapter, private var activity: Activity, private val shopIdx: String, var mainPosition: Int) : RecyclerView.ViewHolder(itemView){
        private lateinit var listener: OnItemClickListener
        private var binding = FragmentImageBinding.bind(itemView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if(listener != null) listener.onItemClick(this, itemView, position)
            }
            itemView.editButton.setOnClickListener{ editPicture() }
            itemView.deleteButton.setOnClickListener{ deletePicture() }
            setIsRecyclable(false)
        }

        fun setItem(uri: String){
            if(uri != ""){
                Glide.with(itemView.context)
                    .load(Uri.parse(uri))
                    .apply(RequestOptions().centerCrop())
                    .into(binding.imageView)
                binding.run{
                    editButton.visibility = View.VISIBLE
                    deleteButton.visibility = View.VISIBLE

                    val params = (imageView.layoutParams as ConstraintLayout.LayoutParams)
                    params.margin = 5
                    imageView.layoutParams = params

                    if(mainPosition == position){
                        Log.e("메인 : $mainPosition", "아이템 : $position")
                        rootLayout.background = activity.getDrawable(R.drawable.fill_primary)
                    }
                }
            }
        }

        fun setOnItemClickListener(listener: OnItemClickListener){
            this.listener = listener
        }

        fun editPicture(){
            (activity as EditPicturesActivity).editPicture(position)
        }

        fun deletePicture(){
            activity.run{
                when {
                    adapter.itemCount == 1 -> toast(getString(R.string.toast_must_have_picture))
                    binding.rootLayout.background != null -> toast(getString(R.string.toast_cannot_remove_main))
                    else -> alert(getString(R.string.dialog_delete_picture)) {
                        positiveButton(getString(R.string.confirm)) {
                            Log.d("포지션", (position).toString())
                            (activity as EditPicturesActivity).deletePicture(position, this@ImageViewHolder)
//                            if(position < mainPosition) (activity as EditPicturesActivity).setAdapterMainPosition(this@ImageViewHolder, position)
                        }
                        negativeButton(getString(R.string.cancel)) {}
                    }.show()
                }
            }
        }

//        fun requestDelete(){
//            val map = HashMap<String, Any>().apply {
//                this[FIELD_SHOP_IDX] = shopIdx
//                this[FIELD_SHOP_PIC_NUM] = position.inc().toString()
//            }
//            retrofitService.postDeleteShopPicture(map)
//                .enqueue(object : Callback<BaseResult> {
//                    override fun onResponse(
//                        call: Call<BaseResult>,
//                        response: Response<BaseResult>
//                    ) {
//                        adapter.run {
//                            removeItem(position)
//                            notifyDataSetChanged()
//                        }
//                    }
//
//                    override fun onFailure(call: Call<BaseResult>, t: Throwable) {
//
//                    }
//                })
//        }
    }
}