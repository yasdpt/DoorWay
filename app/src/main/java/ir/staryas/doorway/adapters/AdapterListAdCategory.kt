package ir.staryas.doorway.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.staryas.doorway.R
import ir.staryas.doorway.model.Categories
import ir.staryas.doorway.model.Category

class AdapterListAdCategory(val context: Context) : RecyclerView.Adapter<AdapterListAdCategory.MyViewHolder>() {

    var categoriesList : List<Categories> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ad_category,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    lateinit var mClickListener: ClickListener

    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    interface ClickListener {
        fun onClick(pos: Int, aView: View)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val cat = categoriesList[position]
        val drwArr = context.resources.obtainTypedArray(R.array.ad_category_icon)
        val obj = Category(cat.categoryId!!.toInt(),cat.categoryName!!,cat.count!!,drwArr.getResourceId(position, -1))
        holder.tvCategoryTitle.text = obj.categoryName
        holder.tvCategoryCount.text = obj.categoryCount + " آگهی"
        holder.ivCatImage.setImageResource(obj.categoryImage)
    }

    fun setAdCategoriesList(categoriesList: List<Categories>){
        this.categoriesList = categoriesList
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener {

        override fun onClick(v: View) {
            mClickListener.onClick(adapterPosition, v)
        }

        var tvCategoryTitle: TextView = itemView!!.findViewById(R.id.title)
        var tvCategoryCount: TextView = itemView!!.findViewById(R.id.tvCatCount)
        var ivCatImage: ImageView = itemView!!.findViewById(R.id.ivCatImage)

        init {
            itemView!!.setOnClickListener(this)
        }
    }


}