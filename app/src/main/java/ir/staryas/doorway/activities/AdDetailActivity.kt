package ir.staryas.doorway.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ir.staryas.doorway.utils.Tools
import kotlinx.android.synthetic.main.activity_ad_detail.*
import saman.zamani.persiandate.PersianDate
import android.net.Uri.fromParts
import android.os.Handler
import android.view.MenuItem
import android.widget.Toast
import ir.staryas.doorway.R
import ir.staryas.doorway.utils.TextViewEx
import ir.staryas.doorway.utils.ViewDialog
import java.text.DecimalFormat
import kotlin.collections.ArrayList


class AdDetailActivity : AppCompatActivity() {

    var adName:String? = null
    var adDesc:String? = null
    var adImages:String? = null
    var adPrice:String? = null
    var createdAt:String? = null
    var categoryName:String? = null
    var cityName:String? = null
    var phone:String? = null

    lateinit var viewDialog: ViewDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_detail)
        initToolbar()
        viewDialog = ViewDialog(this)
        viewDialog.showDialog()
        initComponent()
        Handler().postDelayed({
            viewDialog.hideDialog()
        }, 2000)

    }


    private fun initToolbar() {
        setSupportActionBar(AdDetailToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title="جزییات آگهی"
        MainActivity.tools.setSystemBarColor(this, R.color.grey_5)
        MainActivity.tools.setSystemBarLight(this)
    }

    private fun initComponent(){
        val intent = intent
        val bundle:Bundle? = intent.extras
        adName = bundle!!.getString("ad_name")
        adDesc = bundle.getString("ad_desc")

        adImages = bundle.getString("ad_images")
        adPrice = bundle.getString("ad_price")
        createdAt = bundle.getString("created_at")
        categoryName = bundle.getString("category_name")
        cityName = bundle.getString("city_name")
        phone = bundle.getString("phone")

        val items = ArrayList<String>()

        if (adImages == "null"){
            items.add("http://staryas.ir/doorway/api/images/noimage.png")
        } else {
            var images = adImages!!.split(",")
            images = images.subList(1,images.lastIndex+1)
            for (image in images){

                items.add("http://staryas.ir/doorway/api/images/$image")
            }
        }

        slider.setItems(items)

        val persianDate = PersianDate()
        val tools = Tools()

        tvDetailAdTitle.text = adName
        tvDetailAdTime.text = tools.getTimeDifference(persianDate, createdAt!!)
        tvDetailCat.text = categoryName
        tvDetailCity.text = cityName

        val formatter = DecimalFormat("#,###,###,###")
        val prcstr = adPrice!!.replace(",","").toDouble()
        val formattedPrice:String = formatter.format(prcstr) + " تومان"
        tvDetailPrice.text = formattedPrice
        val tvDesc:TextViewEx = findViewById(R.id.tvDetailDesc)
        val tvPoliceAlert:TextViewEx = findViewById(R.id.tvPoliceAlert)
        tvDesc.setText(adDesc,true)
        tvPoliceAlert.setText(getString(R.string.police_alert),true)

        btnDetailCall.setOnClickListener {
            viewDialog.showDialog()
            Handler().postDelayed({
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse( "tel:$phone")
                startActivity(intent)
                viewDialog.hideDialog()
            }, 2000)

        }

        btnDetailMessage.setOnClickListener {
            viewDialog.showDialog()
            Handler().postDelayed({
                startActivity(Intent(Intent.ACTION_VIEW, fromParts("sms", phone!!, null)))
                viewDialog.hideDialog()
            }, 2000)

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else {
            Toast.makeText(applicationContext, item.title, Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }


}
