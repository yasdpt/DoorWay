package ir.staryas.doorway.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ir.staryas.doorway.R
import ir.staryas.doorway.model.Ads
import ir.staryas.doorway.utils.Tools
import saman.zamani.persiandate.PersianDate
import java.text.DecimalFormat

class AdapterListAdsCat(val context: Context) : RecyclerView.Adapter<AdapterListAdsCat.MyViewHolder>() {

    var adList : MutableList<Ads> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ad,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return adList.size
    }

    lateinit var mClickListener: ClickListener

    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    interface ClickListener {
        fun onClick(pos: Int, aView: View)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.tvAdTitle.text = adList[position].adName
        val formatter = DecimalFormat("#,###,###,###")
        val prcstr = adList[position].adPrice!!.replace(",","").toDouble()
        val formattedPrice:String = formatter.format(prcstr) + " تومان"
        holder.tvPrice.text = formattedPrice
        val images = adList[position].adImages!!.split(",")
        val pdate = PersianDate()
        val createdAt = adList[position].createdAt
        val tools = Tools()
        holder.tvTime.text = tools.getTimeDifference(pdate,createdAt!!)

        var imageUrl = "http://staryas.ir/doorway/api/images/noimage.png"
        if (images[0] != "null")
        {
            imageUrl = "http://staryas.ir/doorway/api/images/" + images[0]
        }
        val requestOption = RequestOptions()
            .placeholder(R.drawable.noimage)
            .centerCrop()
            .transforms(CenterCrop(), RoundedCorners(10))

        Glide.with(context).load(imageUrl)
            .apply(requestOption)
            .into(holder.image)


    }

    fun setAdListItems(adList: MutableList<Ads>){
        this.adList = adList
        notifyDataSetChanged()
    }



    inner class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener {

        override fun onClick(v: View) {
            mClickListener.onClick(adapterPosition, v)
        }

        var tvAdTitle: TextView = itemView!!.findViewById(R.id.title)
        var image: ImageView = itemView!!.findViewById(R.id.ivAddImage)
        var tvPrice: TextView = itemView!!.findViewById(R.id.tvPrice)
        var tvTime: TextView = itemView!!.findViewById(R.id.tvCatCount)

        init {
            itemView!!.setOnClickListener(this)
        }
    }


}