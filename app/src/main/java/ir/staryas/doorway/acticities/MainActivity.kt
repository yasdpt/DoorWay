package ir.staryas.doorway.acticities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import ir.staryas.doorway.*
import ir.staryas.doorway.fragments.*
import ir.staryas.doorway.utils.*

import kotlinx.android.synthetic.main.content_main.*



@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    companion object {
        private const val ID_HOME = 1
        private const val ID_CAT = 2
        private const val ID_ADD = 3
        private const val ID_ACCOUNT = 4
        private const val ID_LOGIN = 5
        private const val ID_ADS_CAT = 6
        private const val ID_MY_ADS = 7
        val tools = Tools()
        var isBackPressed = false
        var isCatFragment = false
        var isAccFragment = false
        private var isUserLoggedIn:Boolean? = false
        lateinit var activityOne:AppCompatActivity
    }

    private lateinit var viewModel: MainViewModel

    lateinit var menuItem:MenuItem

    private lateinit var prefManage: PrefManage
    private lateinit var cityDialog: ViewCityDialog
    private lateinit var viewDialog: ViewDialog

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityOne = this
        initToolbar()
        initComponent()
        initBottomNavigation()
    }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initToolbar() {
        val toolbar:Toolbar = findViewById(R.id.mainToolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.elevation = 0.0f
        tools.setSystemBarColor(this,
            R.color.grey_5
        )
        tools.setSystemBarLight(this)
    }

    private fun initComponent(){
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        prefManage = PrefManage(this)
        cityDialog = ViewCityDialog(this)
        viewDialog = ViewDialog(this)
    }



    private fun initBottomNavigation(){
        bottomNavigation.add(MeowBottomNavigation.Model(
            ID_HOME,
            R.drawable.ic_home
        ))
        bottomNavigation.add(MeowBottomNavigation.Model(
            ID_CAT,
            R.drawable.ic_list_new
        ))
        bottomNavigation.add(MeowBottomNavigation.Model(
            ID_ADD,
            R.drawable.ic_add
        ))
        bottomNavigation.add(MeowBottomNavigation.Model(
            ID_ACCOUNT,
            R.drawable.ic_account
        ))

        bottomNavigation.setOnClickMenuListener {
            Toast.makeText(this,"menu clicked",Toast.LENGTH_SHORT).show()
        }


        if(prefManage.getCityName()=="null"){
            cityDialog.showCityDialog("main")
            cityDialog.dialog.setOnDismissListener {
                //HomeFragment.param = "yasin"
                isAccFragment = false
                isCatFragment = false
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                AdsCatFragment.isAdsFragment = false
                MyAdsFragment.isMyAdsFragment = false
                bottomNavigation.show(1)
                loadFragment(1)
            }
        } else {
            isAccFragment =false
            isCatFragment = false
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            AdsCatFragment.isAdsFragment = false
            MyAdsFragment.isMyAdsFragment = false
            //HomeFragment.param = "yasin"
            bottomNavigation.show(1)
            loadFragment(1)
        }



        title = "همه آگهی ها"

        bottomNavigation.setOnShowListener {

        }


        bottomNavigation.setOnClickMenuListener {
            when (it.id) {
                ID_HOME -> {
                    MyAdsFragment.isMyAdsFragment = false
                    isAccFragment = false
                    supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                    isCatFragment = false
                    AdsCatFragment.isAdsFragment = false
                    title = "همه آگهی ها"
                    loadFragment(it.id)
                    menuItem.isVisible = true
                }
                ID_CAT -> {
                    isAccFragment = false
                    isCatFragment =true
                    if(AdsCatFragment.isAddedOnce) {
                        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                        AdsCatFragment.isAdsFragment = false
                        MyAdsFragment.isMyAdsFragment = false
                        //AdsCatFragment.isAdsFragment = true
                        menuItem.isVisible = true
                        loadFragment(6)
                    }

                    if (!AdsCatFragment.isAddedOnce){
                        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                        AdsCatFragment.isAdsFragment = false
                        MyAdsFragment.isMyAdsFragment = false
                        title = "دسته بندی ها"
                        loadFragment(it.id)
                        menuItem.isVisible = false
                    }
                }
                ID_ADD -> {

                    supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                    isCatFragment = false
                    isAccFragment = false
                    AdsCatFragment.isAdsFragment = false
                    MyAdsFragment.isMyAdsFragment = false
                    title = "ثبت رایگان آگهی"
                    isUserLoggedIn = prefManage.getIsUserLoggedIn()
                    if (!isUserLoggedIn!!)
                    {
                        loadFragment(ID_LOGIN)
                    }
                    else {
                        loadFragment(it.id)
                    }
                    menuItem.isVisible = false
                }
                ID_ACCOUNT -> {
                    isAccFragment = true
                    if (MyAdsFragment.isMyAddedOnce){
                        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                        AdsCatFragment.isAdsFragment = false
                        MyAdsFragment.isMyAdsFragment = false
                        menuItem.isVisible = false
                        loadFragment(7)
                    }

                    if (!MyAdsFragment.isMyAddedOnce){
                        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                        isCatFragment = false
                        MyAdsFragment.isMyAdsFragment = false
                        AdsCatFragment.isAdsFragment = false
                        title = "حساب کاربری"
                        loadFragment(it.id)
                        menuItem.isVisible = false
                    }

                }
            }
        }
    }

    fun loadFragment(itemId: Int) {
        val tag = itemId.toString()
        val fragment = supportFragmentManager.findFragmentByTag(tag) ?: when (itemId) {
            ID_HOME -> {
                HomeFragment.newInstance()
            }
            ID_CAT -> {
                CatFragment.newInstance()
            }
            ID_ADD -> {
                AddFragment.newInstance()
            }
            ID_ACCOUNT -> {
                AccountFragment.newInstance()
            }
            ID_LOGIN -> {
                LoginFragment.newInstance()
            }
            ID_ADS_CAT -> {
                AdsCatFragment.newInstance()
            }
            ID_MY_ADS -> {
                MyAdsFragment.newInstance()
            }
            else -> {
                null
            }
        }

        // show/hide fragment
        if (fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()

            if (viewModel.lastActiveFragmentTag != null) {
                val lastFragment = supportFragmentManager.findFragmentByTag(viewModel.lastActiveFragmentTag)
                if (lastFragment != null)
                {
                    transaction.hide(lastFragment)
                }
            }

            if (!fragment.isAdded) {
                transaction.add(R.id.fragmentContainer, fragment, tag)
            }
            else {
                transaction.show(fragment)
            }

            transaction.commit()
            viewModel.lastActiveFragmentTag = tag
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search_setting, menu)
        tools.changeMenuIconColor(menu, resources.getColor(R.color.grey_60))
        menuItem = menu.findItem(R.id.action_search)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_search) {
                if(AdsCatFragment.isAddedOnce && isCatFragment)
                {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.putExtra("from","cat")
                    intent.putExtra("category", AdsCatFragment.param1)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.putExtra("from","main")
                    startActivity(intent)
                }

        }

        if (item.itemId == android.R.id.home) {
            if(AdsCatFragment.isAddedOnce && isCatFragment)
            {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                isBackPressed = true
                AdsCatFragment.isAddedOnce = false
                AdsCatFragment.isAdsFragment = false
                MyAdsFragment.isMyAddedOnce = false
                MyAdsFragment.isMyAdsFragment = false
                title = "دسته بندی ها"
                loadFragment(2)
                menuItem.isVisible = false

            } else if (MyAdsFragment.isMyAddedOnce && isAccFragment){
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                isBackPressed = true
                AdsCatFragment.isAddedOnce = false
                AdsCatFragment.isAdsFragment = false
                MyAdsFragment.isMyAdsFragment = false
                MyAdsFragment.isMyAddedOnce = false
                title = "حساب کاربری"
                loadFragment(4)
                menuItem.isVisible = false
            } else {
                AdsCatFragment.isAddedOnce = false
                AdsCatFragment.isAdsFragment = false
                MyAdsFragment.isMyAdsFragment = false
                MyAdsFragment.isMyAddedOnce = false
                super.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {

        if(AdsCatFragment.isAddedOnce && isCatFragment)
        {
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            isBackPressed = true
            AdsCatFragment.isAddedOnce = false
            AdsCatFragment.isAdsFragment = false
            MyAdsFragment.isMyAddedOnce = false
            MyAdsFragment.isMyAdsFragment = false
            title = "دسته بندی ها"
            loadFragment(2)
            menuItem.isVisible = false

        } else if (MyAdsFragment.isMyAddedOnce && isAccFragment){
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
            isBackPressed = true
            AdsCatFragment.isAddedOnce = false
            AdsCatFragment.isAdsFragment = false
            MyAdsFragment.isMyAdsFragment = false
            MyAdsFragment.isMyAddedOnce = false
            title = "حساب کاربری"
            loadFragment(4)
            menuItem.isVisible = false
        } else {
            AdsCatFragment.isAddedOnce = false
            AdsCatFragment.isAdsFragment = false
            MyAdsFragment.isMyAdsFragment = false
            MyAdsFragment.isMyAddedOnce = false
            super.onBackPressed()
        }

    }

}
