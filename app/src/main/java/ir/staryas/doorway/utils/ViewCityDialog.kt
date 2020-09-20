package ir.staryas.doorway.utils

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.staryas.doorway.R
import ir.staryas.doorway.adapters.AdapterListAds
import ir.staryas.doorway.adapters.AdapterListCity
import ir.staryas.doorway.model.CitiesResponse
import ir.staryas.doorway.networking.ApiClient
import ir.staryas.doorway.networking.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception


class ViewCityDialog(private var context: Context) {

    lateinit var dialog:Dialog

    private val apiClient = ApiClient()
    private lateinit var tvCityTitle:TextView

    private lateinit var prefManage: PrefManage

    private lateinit var rAdapter:AdapterListCity


    fun showCityDialog(activityName: String){
        dialog = Dialog(context)
        prefManage = PrefManage(context)
        dialog.setContentView(R.layout.fragment_city)
        if (activityName == "main"){
            dialog.setCancelable(false)
        } else {
            dialog.setCancelable(true)
        }

        val lp:WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT

        val rvCity:RecyclerView = dialog.findViewById(R.id.rvCityDialog)
        tvCityTitle = dialog.findViewById(R.id.tvCitySelect)

        rAdapter = AdapterListCity(context)
        rvCity.layoutManager = LinearLayoutManager(context)
        rvCity.adapter = rAdapter
        tvCityTitle.text = "در حال بارگذاری..."
        try {
            getCities(rAdapter)
        } catch (e:Exception){

        }


        rAdapter.setOnItemClickListener(object : AdapterListCity.ClickListener{
            override fun onClick(pos: Int, aView: View) {
                prefManage.setCityId(rAdapter.citiesList[pos].cityId!!.toInt())
                prefManage.setCityName(rAdapter.citiesList[pos].cityName.toString())
                hideCityDialog()
            }

        })

        dialog.show()
        dialog.window!!.attributes = lp

    }

    fun hideCityDialog(){
        dialog.dismiss()
    }





    private fun getCities(recyclerAdapter:AdapterListCity){
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.getCities("city")
        call.enqueue( object : Callback<CitiesResponse> {
            override fun onResponse(call: Call<CitiesResponse>?, response: Response<CitiesResponse>?) {

                val resMsg = response!!.body()!!.success
                if (resMsg == 1){
                    tvCityTitle.text = "لطفا شهر خود را انتخاب کنید"
                    response.body()!!.cities?.let { recyclerAdapter.setCityListItems(it) }
                } else {
                    Toast.makeText(context,R.string.data_error,Toast.LENGTH_SHORT).show()
                    hideCityDialog()
                }

            }

            override fun onFailure(call: Call<CitiesResponse>?, t: Throwable?) {
                showWarningDialog()
            }
        })
    }

    private fun showWarningDialog() {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_warning)
        dialog.setCancelable(false)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        (dialog.findViewById(R.id.btnWarningTry) as AppCompatButton).setOnClickListener {
            getCities(rAdapter)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

}