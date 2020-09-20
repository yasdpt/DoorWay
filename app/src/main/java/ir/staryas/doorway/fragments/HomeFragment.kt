package ir.staryas.doorway.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.staryas.doorway.R
import ir.staryas.doorway.activities.AdDetailActivity
import ir.staryas.doorway.adapters.AdapterListAds
import ir.staryas.doorway.utils.PaginationScrollListener
import ir.staryas.doorway.model.Ads
import ir.staryas.doorway.model.AdsResponse
import ir.staryas.doorway.networking.ApiClient
import ir.staryas.doorway.networking.ApiService
import ir.staryas.doorway.utils.PrefManage
import ir.staryas.doorway.utils.ViewDialog
import kotlinx.android.synthetic.main.fragment_home.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class HomeFragment : Fragment() {

    companion object {

        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }

        private var page = 0
        private var isLastPage: Boolean = false
        private var isLoading: Boolean = false
    }


    private val apiClient = ApiClient()

    private lateinit var adList:MutableList<Ads>

    private lateinit var prefManage: PrefManage
    private lateinit var home:View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        home = inflater.inflate(R.layout.fragment_home, container, false)

        initComponent(home)

        home.swipe_container_home.setOnRefreshListener {
            initComponent(home)
            home.swipe_container_home.isRefreshing = false
        }

        home.btnHomeTryAgain.setOnClickListener {
            initComponent(home)
        }





        return home
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Initialize UI
    }



    private fun initComponent(home: View){
        adList = mutableListOf()
        page = 0
        prefManage = PrefManage(home.context)
        val viewDialog = ViewDialog(activity!!)
        viewDialog.showDialog()
        val recyclerAdapter = AdapterListAds(home.context)
        val layoutManager = LinearLayoutManager(home.context)
        home.rvHome.layoutManager = layoutManager
        home.rvHome.adapter = recyclerAdapter
        val cityId = prefManage.getCityId()
        val llNoItemHome:LinearLayout = home.findViewById(R.id.llNoItemHome)
        val orderBy = "ad_id"
        if (cityId != 0){
            try{
                getAds(recyclerAdapter, cityId!!,
                    page,orderBy,viewDialog,llNoItemHome,home.rvHome)
            } catch (e:Exception){

            }
        }



        home.rvHome.addOnScrollListener(object : PaginationScrollListener(layoutManager) {


            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                isLoading = true
                page++
                viewDialog.showDialog()
                try {
                    getAds(recyclerAdapter,cityId!!,
                        page,orderBy,viewDialog,llNoItemHome,home.rvHome)
                } catch (e:Exception){

                }
            }
        })

        recyclerAdapter.setOnItemClickListener(object : AdapterListAds.ClickListener {
            override fun onClick(pos: Int, aView: View) {
                if (isNetworkAvailable()){
                    val intent = Intent(activity, AdDetailActivity::class.java)
                    intent.putExtra("ad_name",recyclerAdapter.adList[pos].adName)
                    intent.putExtra("ad_desc",recyclerAdapter.adList[pos].adDesc)
                    intent.putExtra("ad_images",recyclerAdapter.adList[pos].adImages)
                    intent.putExtra("ad_price",recyclerAdapter.adList[pos].adPrice)
                    intent.putExtra("created_at",recyclerAdapter.adList[pos].createdAt)
                    intent.putExtra("category_name",recyclerAdapter.adList[pos].categoryName)
                    intent.putExtra("city_name",recyclerAdapter.adList[pos].cityName)
                    intent.putExtra("phone",recyclerAdapter.adList[pos].phone)

                    startActivity(intent)
                } else {
                    Toast.makeText(activity,"لطفا اینترنت خود را متصل و دوباره امتحان کنید!",Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    private fun getAds(recyclerAdapter: AdapterListAds,cityId:Int,page:Int,orderBy:String,viewDialog: ViewDialog,
                       llNoItemHome:LinearLayout,rvHome:RecyclerView){
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.getAdsByCity(cityId, page, orderBy)
        call.enqueue( object : Callback<AdsResponse> {
            override fun onResponse(call: Call<AdsResponse>?, response: Response<AdsResponse>?) {
                isLoading = false
                val resMsg = response!!.body()?.success
                if(resMsg == 1){
                    if(response!!.body()?.ads!!.size!! != 0){
                        viewDialog.hideDialog()
                        llNoItemHome.visibility = View.GONE
                        rvHome.visibility = View.VISIBLE
                        if (response.body()?.ads!!.size!! < 15)
                        {
                            isLastPage = true
                        }
                        if (page==0){
                            adList = response.body()!!.ads!!
                        } else{
                            adList.addAll(adList.lastIndex+1, response.body()!!.ads!!)
                        }
                        adList.let { recyclerAdapter.setAdListItems(it) }

                    } else {
                        viewDialog.hideDialog()
                        rvHome.visibility = View.GONE
                        llNoItemHome.visibility = View.VISIBLE
                    }
                } else {
                    viewDialog.hideDialog()
                    llNoItemHome.visibility = View.VISIBLE
                    rvHome.visibility = View.GONE
                    Toast.makeText(activity, R.string.network_failure_try,Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<AdsResponse>?, t: Throwable?) {

                viewDialog.hideDialog()
                llNoItemHome.visibility = View.VISIBLE
                rvHome.visibility = View.GONE
                Toast.makeText(activity, R.string.network_failure_try,Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }
}