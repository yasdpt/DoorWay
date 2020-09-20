package ir.staryas.doorway.adapters

import ir.staryas.doorway.model.Cities
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.staryas.doorway.R

class AdapterListCity(val context: Context) : RecyclerView.Adapter<AdapterListCity.MyViewHolder>() {

    var citiesList : List<Cities> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return citiesList.size
    }

    lateinit var mClickListener: ClickListener

    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }

    interface ClickListener {
        fun onClick(pos: Int, aView: View)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.tvCityName.text = citiesList[position].cityName

    }

    fun setCityListItems(citiesList: List<Cities>){
        this.citiesList = citiesList
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!), View.OnClickListener {

        override fun onClick(v: View) {
            mClickListener.onClick(adapterPosition, v)
        }

        var tvCityName: TextView = itemView!!.findViewById(R.id.tvCityName)

        init {
            itemView!!.setOnClickListener(this)
        }
    }


}