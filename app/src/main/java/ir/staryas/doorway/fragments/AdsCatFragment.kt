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
import ir.staryas.doorway.adapters.AdapterListAdsCat
import ir.staryas.doorway.utils.PaginationScrollListener
import ir.staryas.doorway.model.Ads
import ir.staryas.doorway.model.AdsResponse
import ir.staryas.doorway.networking.ApiClient
import ir.staryas.doorway.networking.ApiService
import ir.staryas.doorway.utils.PrefManage
import ir.staryas.doorway.utils.ViewDialog
import kotlinx.android.synthetic.main.fragment_ads_cat.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class AdsCatFragment : Fragment() {

    companion object {
        var param1:Int? = 0

        @JvmStatic
        fun newInstance() =
            AdsCatFragment().apply {
                arguments = Bundle().apply {
                    if (param1 != 0){
                        putInt("category", param1!!)
                    }
                }
            }

        var isAddedOnce = false
        var isAdsFragment =false

        var pageAds = 0
        var isLastPageCat: Boolean = false
        var isLoadingCat: Boolean = false
    }


    private val apiClient = ApiClient()

    private lateinit var adListCat:MutableList<Ads>
    lateinit var viewT:View
    lateinit var prefManage: PrefManage


    override fun onHiddenChanged(hidden: Boolean) {

        super.onHiddenChanged(hidden)
        if (isAdsFragment){
            viewT.llNoItemAds.visibility = View.GONE
            adListCat = mutableListOf()
            isLastPageCat = false
            isLoadingCat = false
            pageAds = 0
            isAddedOnce =true
            initComponent(viewT)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt("category")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewT = inflater.inflate(R.layout.fragment_ads_cat, container, false)

        adListCat = mutableListOf()
        isLastPageCat = false
        isLoadingCat = false
        pageAds = 0
        isAddedOnce =true
        initComponent(viewT)
        viewT.swipe_container_2.setOnRefreshListener {
            adListCat = mutableListOf()
            isLastPageCat = false
            isLoadingCat = false
            pageAds = 0
            initComponent(viewT)
            viewT.swipe_container_2.isRefreshing = false
        }

        viewT.btnAdsCatTryAgain.setOnClickListener {

            adListCat = mutableListOf()
            isLastPageCat = false
            isLoadingCat = false
            pageAds = 0
            initComponent(viewT)
        }


        return viewT
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Initialize UI

    }



    fun initComponent(view: View){
        prefManage = PrefManage(view.context)
        val viewDialog = ViewDialog(activity)
        viewDialog.showDialog()
        val recyclerAdapter = AdapterListAdsCat(view.context)
        val layoutManager = LinearLayoutManager(view.context)
        view.rvAdsCat.layoutManager = layoutManager
        view.rvAdsCat.adapter = recyclerAdapter
        val cityId = prefManage.getCityId()
        val orderBy = "ad_id"
        var llNoItemAds:LinearLayout = view.findViewById(R.id.llNoItemAds)
        //Toast.makeText(view.context, param,Toast.LENGTH_LONG).show()
        if (cityId != 0){
            if (param1 !=0){
                try {
                    getAdsByCat(recyclerAdapter,cityId!!, param1!!,
                        pageAds,orderBy,viewDialog,llNoItemAds,view.rvAdsCat)
                } catch (e:Exception){

                }
            }
        }

        view.rvAdsCat.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return isLastPageCat
            }

            override fun isLoading(): Boolean {
                return isLoadingCat
            }

            override fun loadMoreItems() {
                isLoadingCat = true
                pageAds++
                viewDialog.showDialog()
                try {
                    getAdsByCat(recyclerAdapter,cityId!!, param1!!,
                        pageAds,orderBy,viewDialog,llNoItemAds,view.rvAdsCat)
                } catch (e:Exception){

                }
            }
        })

        recyclerAdapter.setOnItemClickListener(object : AdapterListAdsCat.ClickListener {
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




    fun getAdsByCat(recyclerAdapter: AdapterListAdsCat, cityId: Int, categoryId:Int, page: Int,
                    orderBy: String, viewDialog: ViewDialog, llNoItemAds:LinearLayout,rvAds:RecyclerView){
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.getAdsByCategory(cityId, page, orderBy,categoryId)
        call.enqueue( object : Callback<AdsResponse> {
            override fun onResponse(call: Call<AdsResponse>?, response: Response<AdsResponse>?) {
                isLoadingCat = false
                viewDialog.hideDialog()
                val resMsg = response!!.body()?.success
                if (resMsg == 1){
                    if (response.body()!!.ads!!.size!! != 0){
                        llNoItemAds.visibility = View.GONE
                        rvAds.visibility = View.VISIBLE
                        if (response!!.body()?.ads!!.size!! < 15)
                        {
                            isLastPageCat = true
                        }
                        if (page==0){
                            adListCat = response.body()!!.ads!!
                        } else{
                            adListCat.addAll(adListCat.lastIndex+1, response.body()!!.ads!!)
                        }
                        adListCat.let { recyclerAdapter.setAdListItems(it) }
                    } else {
                        rvAds.visibility = View.GONE
                        llNoItemAds.visibility = View.VISIBLE
                    }
                } else {
                    rvAds.visibility = View.GONE
                    llNoItemAds.visibility = View.VISIBLE
                    Toast.makeText(activity, R.string.data_error,Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AdsResponse>?, t: Throwable?) {
                rvAds.visibility = View.GONE
                llNoItemAds.visibility = View.VISIBLE
                Toast.makeText(activity, R.string.network_failure,Toast.LENGTH_SHORT).show()
                viewDialog.hideDialog()
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

    override fun onDestroy() {
        super.onDestroy()

    }


}