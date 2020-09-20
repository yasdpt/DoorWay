package ir.staryas.doorway.acticities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ir.staryas.doorway.adapters.AdapterSuggestionSearch
import ir.staryas.doorway.utils.Tools
import kotlinx.android.synthetic.main.activity_search.*
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import ir.staryas.doorway.utils.ViewAnimation
import android.view.inputmethod.EditorInfo
import android.view.WindowManager
import ir.staryas.doorway.R
import ir.staryas.doorway.adapters.AdapterListAds
import ir.staryas.doorway.utils.PaginationScrollListener
import ir.staryas.doorway.model.Ads
import ir.staryas.doorway.model.AdsResponse
import ir.staryas.doorway.networking.ApiClient
import ir.staryas.doorway.networking.ApiService
import ir.staryas.doorway.utils.PrefManage
import ir.staryas.doorway.utils.ViewDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class SearchActivity : AppCompatActivity() {

    private val tools = Tools()
    private var from:String? = "null"
    private var category:Int? = 0

    private val apiClient = ApiClient()

    private lateinit var mAdapterSuggestionSearch: AdapterSuggestionSearch


    private lateinit var adList:MutableList<Ads>
    private var isLastPage: Boolean = false
    private var isLoading: Boolean = false
    private var page = 0
    private lateinit var recyclerAdapter:AdapterListAds
    private lateinit var prefManage: PrefManage
    private var cityId: Int = 0
    private val orderBy = "ad_id"
    private lateinit var viewDialog:ViewDialog
    private var lastQuery:String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val intent = intent
        val bundle:Bundle? = intent.extras
        from = bundle!!.getString("from")
        if (from =="cat"){
            category = bundle.getInt("category")
        }
        initToolbar()
        initComponent()
    }


    private fun initToolbar() {

        setSupportActionBar(searchToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        tools.setSystemBarColor(this,
            R.color.grey_5
        )
        tools.setSystemBarLight(this)
    }

    private fun initComponent() {

        adList = mutableListOf()
        isLastPage = false
        isLoading = false
        page = 0

        prefManage = PrefManage(this)
        viewDialog = ViewDialog(this)

        cityId = prefManage.getCityId()!!

        recyclerAdapter = AdapterListAds(this)
        val layoutManager = LinearLayoutManager(this)

        rvSearch.layoutManager = layoutManager
        rvSearch.adapter = recyclerAdapter

        rvSearch.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                isLoading = true
                page++
                //progress_bar.visibility = View.VISIBLE
                viewDialog.showDialog()
                if (from == "main"){
                    try {
                        if (cityId != 0){
                            getAds(recyclerAdapter,
                                from!!,lastQuery,cityId,page,orderBy)
                        }

                    } catch (e:Exception){

                    }
                } else {
                    try {
                        getAdsByCat(recyclerAdapter, from!!,lastQuery,cityId, category!!,page,orderBy)
                    } catch (e:Exception){

                    }
                }
            }
        })

        et_search.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.toString().trim().isEmpty()) {
                    bt_clear.visibility = View.GONE
                } else {
                    bt_clear.visibility = View.VISIBLE
                }
            }
        })

        recyclerAdapter.setOnItemClickListener(object : AdapterListAds.ClickListener {
            override fun onClick(pos: Int, aView: View) {
                if (isNetworkAvailable()){
                    val intent = Intent(this@SearchActivity, AdDetailActivity::class.java)
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
                    Toast.makeText(this@SearchActivity,"لطفا اینترنت خود را متصل و دوباره امتحان کنید!",Toast.LENGTH_SHORT).show()
                }
            }
        })

        bt_clear.visibility = View.GONE
        recyclerSuggestion.layoutManager = LinearLayoutManager(this)
        recyclerSuggestion.hasFixedSize()

        mAdapterSuggestionSearch = AdapterSuggestionSearch(this)
        recyclerSuggestion.adapter = mAdapterSuggestionSearch
        showSuggestionSearch()
        mAdapterSuggestionSearch.setOnItemClickListener { view, viewModel, pos ->
            et_search.setText(viewModel)
            ViewAnimation.collapse(lyt_suggestion)
            hideKeyboard()
            searchAction()
        }

        bt_clear.setOnClickListener {
            et_search.setText("")
        }

        et_search.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard()
                    searchAction()
                    true
                }
                false
        }

        et_search.setOnTouchListener { v, event ->
            mAdapterSuggestionSearch.refreshItems()
            ViewAnimation.expand(lyt_suggestion)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            false
        }


    }

    private fun showSuggestionSearch() {

    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun searchAction() {
        viewDialog.showDialog()
        //progress_bar.visibility = View.VISIBLE
        rvSearch.visibility = View.GONE
        ViewAnimation.collapse(lyt_suggestion)
        lyt_no_result.visibility = View.GONE
        val query: String = et_search.text.toString().trim()

        if (query != "") {
            lastQuery = query
            if (from == "main"){
                try {
                    adList = mutableListOf()
                    if (cityId != 0){

                        getAds(recyclerAdapter, from!!,query,cityId,page,orderBy)
                    }

                } catch (e:Exception){

                }
            } else {
                try {
                    adList = mutableListOf()
                    getAdsByCat(recyclerAdapter, from!!,query,cityId, category!!,page,orderBy)
                } catch (e:Exception){

                }
            }
            mAdapterSuggestionSearch.addSearchHistory(query)
        } else {
            progress_bar.visibility = View.GONE
            Toast.makeText(this, "لطفا فیلد را پر کنید", Toast.LENGTH_SHORT).show()
        }



    }

    private fun getAds(recyclerAdapter: AdapterListAds,mode:String,query:String, cityId:Int, page:Int, orderBy:String){
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.searchAdsByCity(mode,query,cityId, page, orderBy)
        call.enqueue( object : Callback<AdsResponse> {
            override fun onResponse(call: Call<AdsResponse>?, response: Response<AdsResponse>?) {
                isLoading = false
                viewDialog.hideDialog()
                val resMsg = response!!.body()?.success
                if(resMsg == 1){
                    lyt_no_result.visibility = View.GONE
                    rvSearch.visibility = View.VISIBLE
                    if(response!!.body()?.ads!!.size!! != 0){
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
                        rvSearch.visibility = View.GONE
                        lyt_no_result.visibility = View.VISIBLE
                    }
                } else {
                    rvSearch.visibility = View.GONE
                    lyt_no_result.visibility = View.VISIBLE
                    Toast.makeText(applicationContext, R.string.network_failure_try,Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<AdsResponse>?, t: Throwable?) {
                viewDialog.hideDialog()
                rvSearch.visibility = View.GONE
                lyt_no_result.visibility = View.VISIBLE
                Toast.makeText(applicationContext, R.string.network_failure_try,Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getAdsByCat(recyclerAdapter: AdapterListAds,mode:String,query:String, cityId: Int, categoryId:Int, page: Int,
                            orderBy: String){
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.searchAdsByCategory(mode,query,cityId, page, orderBy,categoryId)
        call.enqueue( object : Callback<AdsResponse> {
            override fun onResponse(call: Call<AdsResponse>?, response: Response<AdsResponse>?) {
                isLoading = false
                viewDialog.hideDialog()
                val resMsg = response!!.body()?.success
                if (resMsg == 1){
                    lyt_no_result.visibility = View.GONE
                    rvSearch.visibility = View.VISIBLE
                    if (response.body()!!.ads!!.size!! != 0){
                        if (response!!.body()?.ads!!.size!! < 15)
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
                        lyt_no_result.visibility = View.VISIBLE
                        rvSearch.visibility = View.GONE
                    }
                } else {
                    lyt_no_result.visibility = View.VISIBLE
                    rvSearch.visibility = View.GONE
                    Toast.makeText(applicationContext, R.string.data_error,Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AdsResponse>?, t: Throwable?) {
                viewDialog.hideDialog()
                rvSearch.visibility = View.GONE
                lyt_no_result.visibility = View.VISIBLE
                Toast.makeText(applicationContext, R.string.network_failure,Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else {
            Toast.makeText(applicationContext, item.title, Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }


}
