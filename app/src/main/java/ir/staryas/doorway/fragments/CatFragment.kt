package ir.staryas.doorway.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.staryas.doorway.model.CategoriesResponse
import ir.staryas.doorway.networking.ApiClient
import ir.staryas.doorway.networking.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ir.staryas.doorway.adapters.AdapterListAdCategory
import ir.staryas.doorway.utils.PrefManage
import ir.staryas.doorway.utils.ViewDialog
import java.lang.Exception
import androidx.appcompat.widget.AppCompatButton
import android.app.Dialog
import android.view.*
import ir.staryas.doorway.R
import ir.staryas.doorway.activities.MainActivity


class CatFragment : Fragment() {

    companion object {

        @JvmStatic
        fun newInstance() =
            CatFragment().apply {
                arguments = Bundle().apply {
                    // putString(ARG_PARAM1, param1)
                }

            }

        var cityId = 0

    }

    private val apiClient = ApiClient()

    private lateinit var viewOne:View
    private lateinit var viewDialog: ViewDialog
    private lateinit var prefManage: PrefManage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewOne = inflater.inflate(R.layout.fragment_cat, container, false)
        // Inflate the layout for this fragment
        initComponent(viewOne)
        //showWarningDialog()
        return viewOne
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Initialize UI
    }



    private fun initComponent(view:View) {
        prefManage = PrefManage(view.context)
        viewDialog = ViewDialog(view.context)
        viewDialog.showDialog()

        cityId = prefManage.getCityId()!!
        val recyclerView: RecyclerView = view.findViewById(R.id.rvCategory)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.hasFixedSize()
        recyclerView.isNestedScrollingEnabled = false
        val mAdapter = AdapterListAdCategory(view.context)
        try {
            getCategories(mAdapter)
        } catch (e:Exception){

        }
        recyclerView.adapter = mAdapter

        mAdapter.setOnItemClickListener(object : AdapterListAdCategory.ClickListener{
            override fun onClick(pos: Int, aView: View) {
                //Toast.makeText(view.context, mAdapter.categoriesList[pos].categoryName+" کلیک شد",Toast.LENGTH_SHORT).show()
                val activity: MainActivity = activity as MainActivity
                AdsCatFragment.param1 = mAdapter.categoriesList[pos].categoryId!!.toInt()
                activity.menuItem.isVisible = true
                activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                AdsCatFragment.isAdsFragment =true
                activity.title = mAdapter.categoriesList[pos].categoryName
                activity.loadFragment(6)

                }


        })
    }

    private fun getCategories(mAdapter: AdapterListAdCategory){
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.getCategories("priority", cityId)
        call.enqueue( object : Callback<CategoriesResponse> {
            override fun onResponse(call: Call<CategoriesResponse>?, response: Response<CategoriesResponse>?) {

                val resMsg = response!!.body()!!.success

                if (resMsg == 1){
                    val categories = response.body()!!.categories!!
                    mAdapter.setAdCategoriesList(categories)
                    viewDialog.hideDialog()
                } else {
                    viewDialog.hideDialog()
                    showWarningDialog()
                }



            }

            override fun onFailure(call: Call<CategoriesResponse>?, t: Throwable?) {
                viewDialog.hideDialog()
                showWarningDialog()
            }
        })
    }

    private fun showWarningDialog() {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_warning)
        dialog.setCancelable(false)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        (dialog.findViewById(R.id.btnWarningTry) as AppCompatButton).setOnClickListener { v ->
            initComponent(viewOne)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window.attributes = lp
    }
}