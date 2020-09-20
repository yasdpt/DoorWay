package ir.staryas.doorway.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.fragment.app.Fragment
import ir.staryas.doorway.utils.PrefManage
import ir.staryas.doorway.utils.ViewCityDialog
import ir.staryas.doorway.utils.ViewDialog
import kotlinx.android.synthetic.main.fragment_account.view.*
import android.app.Dialog
import android.widget.ImageButton
import android.widget.TextView
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.*
import ir.staryas.doorway.BuildConfig
import ir.staryas.doorway.R
import ir.staryas.doorway.activities.LoginActivity
import ir.staryas.doorway.activities.MainActivity
import ir.staryas.doorway.utils.TextViewEx
import java.lang.Exception


class AccountFragment : Fragment() {

    companion object {

        @JvmStatic
        fun newInstance() =
            AccountFragment().apply {
                arguments = Bundle().apply {
                    // putString(ARG_PARAM1, param1)
                }
            }


        var currentCity = ""
    }

    lateinit var prefManage: PrefManage
    lateinit var cityDialog: ViewCityDialog
    lateinit var viewDialog: ViewDialog

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
        val view = inflater.inflate(ir.staryas.doorway.R.layout.fragment_account, container, false)
        // Inflate the layout for this fragment
        initComponent(view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Initialize UI
    }

    private fun initComponent(view: View){
        prefManage = PrefManage(view.context)
        viewDialog = ViewDialog(activity)
        cityDialog = ViewCityDialog(view.context)
        currentCity = prefManage.getCityName().toString()
        var isUserLoggedIn:Boolean = prefManage.getIsUserLoggedIn()!!
        view.tvAccCity.text = prefManage.getCityName()
        if (isUserLoggedIn){
            hideLogin(view)
        } else {
            showLogin(view)
        }

        view.btnAccLogOut.setOnClickListener {
            isUserLoggedIn = prefManage.getIsUserLoggedIn()!!
            if (isUserLoggedIn){
                viewDialog.showDialog()
                Handler().postDelayed({
                    prefManage.setIsUserLoggedIn(false)
                    showLogin(view)
                    viewDialog.hideDialog()
                }, 2000)
            } else {
                val intent = Intent(view.context, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        view.btnMyCity.setOnClickListener {
            cityDialog.showCityDialog("account")
            cityDialog.dialog.setOnDismissListener {
                view.tvAccCity.text = prefManage.getCityName()
                if (currentCity != prefManage.getCityName()){
                    val intent = activity!!.intent
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    activity!!.overridePendingTransition(0, 0)
                    activity!!.finish()
                    activity!!.overridePendingTransition(0, 0)
                    startActivity(intent)
                }
            }
        }

        view.btnMyAds.setOnClickListener {
            if (prefManage.getIsUserLoggedIn()!!){
                val activity: MainActivity = activity as MainActivity
                activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                MyAdsFragment.isMyAdsFragment =true
                activity.title = "آگهی های من"
                activity.loadFragment(7)
            } else {
                Toast.makeText(activity,"لطفا ابتدا به حساب کاربری خود وارد شوید",Toast.LENGTH_SHORT).show()
            }
        }

        view.btnAboutUs.setOnClickListener {
            showDialogAbout()
        }

        view.btnPrivacyPolicy.setOnClickListener {
            try {
                showDialogPrivacy()
            } catch (e:Exception){

            }
        }
    }



    private fun showLogin(view:View) {
        view.btnAccLogOut.text = "ورود"
        view.tvAccFullName.text = ""
        view.tvAccUsername.text = ""
        view.tvAccEmail.text = ""
        view.tvAccUserTitle.text = ""
        view.tvAccEmailTitle.text = ""
        view.tvAccNameTitle.text = ""
        view.tvAccLogin.visibility = View.VISIBLE
        view.tvAccLogin.text = "برای استفاده از تمام امکانات برنامه لطفا به حساب خود وارد شوید"
    }

    private fun hideLogin(view: View){
        view.btnAccLogOut.text = "خروج از حساب"
        view.tvAccFullName.text = prefManage.getFullName()
        view.tvAccUsername.text = prefManage.getUsername()
        view.tvAccEmail.text = prefManage.getEmail()
        view.tvAccLogin.visibility = View.GONE
    }

    private fun showDialogAbout() {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_about)
        dialog.setCancelable(true)
        dialog.window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        (dialog.findViewById(R.id.tvAboutText) as TextViewEx).setText(getString(R.string.about_text),true)

        (dialog.findViewById(R.id.tv_version) as TextView).text = "Version " + BuildConfig.VERSION_NAME

        (dialog.findViewById(R.id.bt_getcode) as View).setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("https://staryas.ir")
            startActivity(i)
        }

        (dialog.findViewById(R.id.bt_close) as ImageButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window.attributes = lp
    }

    private fun showDialogPrivacy(){
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_privacy)
        dialog.setCancelable(true)
        dialog.window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        (dialog.findViewById(R.id.tvPrivacy) as TextViewEx).setText(getString(R.string.rules),true)


        (dialog.findViewById(R.id.bt_close) as ImageButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window.attributes = lp
    }
}