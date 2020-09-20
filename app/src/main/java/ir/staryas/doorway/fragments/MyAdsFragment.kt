package ir.staryas.doorway.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.staryas.doorway.R
import ir.staryas.doorway.activities.AdDetailActivity
import ir.staryas.doorway.activities.MainActivity
import ir.staryas.doorway.adapters.AdapterListAdsUser
import ir.staryas.doorway.utils.PaginationScrollListener
import ir.staryas.doorway.model.Ads
import ir.staryas.doorway.model.AdsResponse
import ir.staryas.doorway.model.MSG
import ir.staryas.doorway.networking.ApiClient
import ir.staryas.doorway.networking.ApiService
import ir.staryas.doorway.utils.PrefManage
import ir.staryas.doorway.utils.ViewDialog
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_my_ads.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class MyAdsFragment : Fragment() {

    companion object {
        //var param1:Int? = 0

        @JvmStatic
        fun newInstance() =
            MyAdsFragment().apply {
                arguments = Bundle().apply {
                    /*if (param1 != 0){
                        putInt("category", param1!!)
                    }*/
                }
            }

        var isMyAddedOnce = false
        var isMyAdsFragment =false



        var pageAdsUser = 0
        var isLastPageUser: Boolean = false
        var isLoadingUser: Boolean = false
    }


    private val apiClient = ApiClient()

    private lateinit var adListUser:MutableList<Ads>

    private lateinit var viewS:View
    private lateinit var prefManage: PrefManage
    private lateinit var manageAdDialog:Dialog


    override fun onHiddenChanged(hidden: Boolean) {

        super.onHiddenChanged(hidden)
        if (isMyAdsFragment){
            viewS.llNoItemMyAds.visibility = View.GONE
            adListUser = mutableListOf()
            isLastPageUser = false
            isLoadingUser = false
            pageAdsUser = 0
            isMyAddedOnce =true
            initComponent(viewS)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //param1 = it.getInt("category")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewS = inflater.inflate(R.layout.fragment_my_ads, container, false)

        adListUser = mutableListOf()
        isLastPageUser = false
        isLoadingUser = false
        pageAdsUser = 0
        isMyAddedOnce =true
        initComponent(viewS)
        viewS.swipe_container_4.setOnRefreshListener {
            viewS.llNoItemMyAds.visibility = View.GONE
            adListUser = mutableListOf()
            isLastPageUser = false
            isLoadingUser = false
            pageAdsUser = 0
            initComponent(viewS)
            viewS.swipe_container_4.isRefreshing = false
        }

        viewS.btnMyAdsSendAd.setOnClickListener {
            viewS.llNoItemMyAds.visibility = View.GONE
            adListUser = mutableListOf()
            isLastPageUser = false
            isLoadingUser = false
            pageAdsUser = 0
            initComponent(viewS)
        }


        return viewS
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Initialize UI

    }



    fun initComponent(viewSecond: View){
        prefManage = PrefManage(viewSecond.context)
        val viewDialog = ViewDialog(activity)
        viewDialog.showDialog()
        val recyclerAdapter = AdapterListAdsUser(viewSecond.context)
        val userLayoutManager = LinearLayoutManager(viewSecond.context)
        viewSecond.rvAdsUser.layoutManager = userLayoutManager
        viewSecond.rvAdsUser.adapter = recyclerAdapter
        val userId = prefManage.getUserId()
        val username = prefManage.getUsername()
        viewSecond.tvMyAdsLog.text = "شما با نام کاربری " + username + " وارد شده اید و آگهی های ثبت شده با این اکانت را مشاهده می کنید."
        val orderBy = "ad_id"
        val llNoItemMyAds:LinearLayout = viewSecond.findViewById(R.id.llNoItemMyAds)
        //Toast.makeText(view.context, param,Toast.LENGTH_LONG).show()
        if (userId != 0){
            // TODO: implement get my ads
            try {
                getMyAds(recyclerAdapter, userId!!,
                    pageAdsUser,orderBy,viewDialog,llNoItemMyAds,viewSecond.rvAdsUser)
            } catch (e:Exception){

            }

        }

        viewSecond.rvAdsUser.addOnScrollListener(object : PaginationScrollListener(userLayoutManager) {
            override fun isLastPage(): Boolean {
                return isLastPageUser
            }

            override fun isLoading(): Boolean {
                return isLoadingUser
            }

            override fun loadMoreItems() {
                isLoadingUser = true
                pageAdsUser++
                viewDialog.showDialog()
                try {
                    getMyAds(recyclerAdapter, userId!!,
                        pageAdsUser,orderBy,viewDialog,llNoItemMyAds,viewSecond.rvAdsUser)
                } catch (e:Exception){

                }

            }
        })

        recyclerAdapter.setOnItemClickListener(object : AdapterListAdsUser.ClickListener {
            override fun onClick(pos: Int, aView: View) {
                showManageAdDialog(viewSecond,recyclerAdapter,pos,viewDialog)
            }
        })
    }

    fun showManageAdDialog(view: View,recyclerAdapter: AdapterListAdsUser,pos:Int,viewDialog: ViewDialog){
        manageAdDialog = Dialog(view.context)
        manageAdDialog.setContentView(R.layout.sheet_ad_info)
        manageAdDialog.setCancelable(true)

        var lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(manageAdDialog.window.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        val btnAdPreview:LinearLayout = manageAdDialog.findViewById(R.id.btnAdPreview)
        val btnDeleteAd:LinearLayout = manageAdDialog.findViewById(R.id.btnDeleteAd)

        btnAdPreview.setOnClickListener {
            if (isNetworkAvailable()){
                val intent = Intent(activity, AdDetailActivity::class.java)
                intent.putExtra("ad_name",recyclerAdapter.adListUser[pos].adName)
                intent.putExtra("ad_desc",recyclerAdapter.adListUser[pos].adDesc)
                intent.putExtra("ad_images",recyclerAdapter.adListUser[pos].adImages)
                intent.putExtra("ad_price",recyclerAdapter.adListUser[pos].adPrice)
                intent.putExtra("created_at",recyclerAdapter.adListUser[pos].createdAt)
                intent.putExtra("category_name",recyclerAdapter.adListUser[pos].categoryName)
                intent.putExtra("city_name",recyclerAdapter.adListUser[pos].cityName)
                intent.putExtra("phone",recyclerAdapter.adListUser[pos].phone)

                startActivity(intent)
                hideManageAdDialog()
            } else {
                Toast.makeText(activity,"لطفا اینترنت خود را متصل و دوباره امتحان کنید!",Toast.LENGTH_SHORT).show()
            }
        }

        btnDeleteAd.setOnClickListener {
            viewDialog.showDialog()
            if (isNetworkAvailable()){
                try {
                    deleteAd("delete",recyclerAdapter.adListUser[pos].adId!!.toInt(),viewDialog)
                    hideManageAdDialog()
                } catch (e:Exception){

                }
            } else {
                Toast.makeText(activity,"لطفا اینترنت خود را متصل و دوباره امتحان کنید!",Toast.LENGTH_SHORT).show()
            }
        }


        manageAdDialog.show()
        manageAdDialog.window.attributes = lp

    }

    fun hideManageAdDialog(){
        manageAdDialog.dismiss()
    }

    private fun getMyAds(recyclerAdapter: AdapterListAdsUser, userId: Int, page: Int,
                         orderBy: String, viewDialog: ViewDialog, llNoItemMyAds:LinearLayout,rvAdsUser:RecyclerView) {

        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.getAdsByUser(userId, page, orderBy)
        call.enqueue( object : Callback<AdsResponse> {
            override fun onResponse(call: Call<AdsResponse>?, response: Response<AdsResponse>?) {
                isLoadingUser = false
                viewDialog.hideDialog()
                val resMsg = response!!.body()?.success
                if (resMsg == 1){
                    if (response.body()!!.ads!!.size!! != 0){
                        llNoItemMyAds.visibility = View.GONE
                        rvAdsUser.visibility = View.VISIBLE
                        if (response!!.body()?.ads!!.size!! < 10)
                        {
                            isLastPageUser = true
                        }
                        if (page==0){
                            adListUser = response.body()!!.ads!!
                        } else{
                            adListUser.addAll(adListUser.lastIndex+1, response.body()!!.ads!!)
                        }
                        adListUser.let { recyclerAdapter.setAdListUserItems(it) }
                    } else {
                        rvAdsUser.visibility = View.GONE
                        llNoItemMyAds.visibility = View.VISIBLE
                        viewS.tvMyAdsNotFound.text = viewS.tvMyAdsNotFound.text.toString() + "\nاولین آگهی خود را ارسال کنید!"
                        viewS.btnMyAdsSendAd.text = "ارسال آگهی رایگان"
                        viewS.btnMyAdsSendAd.setOnClickListener {
                            val activity: MainActivity = activity as MainActivity
                            activity.supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                            MainActivity.isCatFragment = false
                            MainActivity.isAccFragment = false
                            AdsCatFragment.isAdsFragment = false
                            isMyAdsFragment = false
                            activity.menuItem.isVisible = false
                            activity.title = "ثبت رایگان آگهی"
                            activity.bottomNavigation.show(3)
                            activity.loadFragment(6)
                        }
                    }
                } else {
                    viewS.tvMyAdsNotFound.text = "آیتمی پیدا نشد"
                    viewS.btnMyAdsSendAd.text = "تلاش دوباره"
                    viewS.btnMyAdsSendAd.setOnClickListener {
                        viewS.llNoItemMyAds.visibility = View.GONE
                        adListUser = mutableListOf()
                        isLastPageUser = false
                        isLoadingUser = false
                        pageAdsUser = 0
                        isMyAddedOnce =true
                        initComponent(viewS)
                    }
                    rvAdsUser.visibility = View.GONE
                    llNoItemMyAds.visibility = View.VISIBLE
                    Toast.makeText(activity, R.string.network_failure_try,Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AdsResponse>?, t: Throwable?) {
                isLoadingUser = false
                rvAdsUser.visibility = View.GONE
                llNoItemMyAds.visibility = View.VISIBLE
                viewS.tvMyAdsNotFound.text = "آیتمی پیدا نشد"
                viewS.btnMyAdsSendAd.text = "تلاش دوباره"
                viewS.btnMyAdsSendAd.setOnClickListener {
                    viewS.llNoItemMyAds.visibility = View.GONE
                    adListUser = mutableListOf()
                    isLastPageUser = false
                    isLoadingUser = false
                    pageAdsUser = 0
                    isMyAddedOnce =true
                    initComponent(viewS)
                }
                Toast.makeText(activity, R.string.network_failure,Toast.LENGTH_SHORT).show()
                viewDialog.hideDialog()
            }
        })

    }

    private fun deleteAd(mode:String,adId:Int,viewDialog: ViewDialog) {
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.deleteAd(mode, adId)

        call.enqueue(object : Callback<MSG>{

            override fun onResponse(call: Call<MSG>, response: Response<MSG>) {
                viewDialog.hideDialog()
                val resMsg = response!!.body()?.success
                if (resMsg == 1){
                    Toast.makeText(activity, response!!.body()?.message,Toast.LENGTH_SHORT).show()
                    viewS.llNoItemMyAds.visibility = View.GONE
                    adListUser = mutableListOf()
                    isLastPageUser = false
                    isLoadingUser = false
                    pageAdsUser = 0
                    initComponent(viewS)
                } else {
                    Toast.makeText(activity, response!!.body()?.message,Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MSG>, t: Throwable) {
                viewDialog.hideDialog()
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

    override fun onDestroy() {
        super.onDestroy()

    }


}