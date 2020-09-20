package ir.staryas.doorway.acticities

import android.app.Dialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import ir.staryas.doorway.networking.ApiClient
import ir.staryas.doorway.networking.ApiService
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.net.NetworkInfo
import android.content.Intent
import android.net.ConnectivityManager
import android.os.PatternMatcher
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputLayout
import ir.staryas.doorway.R
import ir.staryas.doorway.model.MSG
import ir.staryas.doorway.model.USRMSG
import ir.staryas.doorway.utils.PrefManage
import ir.staryas.doorway.utils.ViewDialog
import java.lang.Exception
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() {


    companion object {
        lateinit var activityThree:AppCompatActivity
    }

    private val apiClient = ApiClient()
    private lateinit var viewDialog:ViewDialog
    private lateinit var prefManage: PrefManage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        activityThree = this
        initToolbar()
        initComponent()
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.signUpToolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title="ورود"
        MainActivity.tools.setSystemBarColor(this,
            R.color.grey_5
        )
        MainActivity.tools.setSystemBarLight(this)
    }

    private fun initComponent(){
        viewDialog = ViewDialog(this)
        prefManage = PrefManage(this)


        btnLogin.setOnClickListener {
            if (isNetworkAvailable()) {
                val username = edUsernameLogin.text.toString().trim()
                val password = edPasswordLogin.text.toString().trim()


                try {
                    if (validate(username,password))
                    {
                        btnLogin.isEnabled = false
                        viewDialog.showDialog()
                        login(username,password)
                    }
                } catch (e:Exception){

                }
            } else {
                Toast.makeText(this,"لطفا اینترنت خود را متصل و دوباره تلاش کنید",Toast.LENGTH_LONG).show()
            }
        }

        btnLoginSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnForgotPassword.setOnClickListener {

            try {
                showRecoveryDialog()
            } catch (e:Exception){
            }
        }

    }

    private fun login(username:String,password:String) {

        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.login(username, password)

        call.enqueue(object : Callback<USRMSG> {
            override fun onResponse(call: Call<USRMSG>, response: Response<USRMSG>) {
                if (response.body()?.success == 1) {
                    prefManage.setIsUserLoggedIn(true)
                    prefManage.setUsername(username)
                    prefManage.setFullName(response.body()?.user!![0].fullName.toString())
                    prefManage.setEmail(response.body()?.user!![0].email.toString())
                    prefManage.setPhone(response.body()?.user!![0].phone.toString())
                    prefManage.setUserId(response.body()?.user!![0].userId!!.toInt())
                    viewDialog.hideDialog()
                    Toast.makeText(applicationContext, response.body()?.message, Toast.LENGTH_LONG).show()

                    try {
                        if (MainActivity.activityOne != null){
                            MainActivity.activityOne.finish()
                        }
                        if (SignUpActivity.activityTwo != null){
                            SignUpActivity.activityTwo.finish()
                        }
                    } catch (e:Exception){

                    }

                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)

                    finish()
                } else {
                    viewDialog.hideDialog()
                    Toast.makeText(applicationContext, response.body()?.message, Toast.LENGTH_LONG).show()
                    btnLogin.isEnabled = true
                }
            }

            override fun onFailure(call: Call<USRMSG>, t: Throwable) {
                Toast.makeText(applicationContext,"خطا در ارتباط با سرور",Toast.LENGTH_LONG).show()
                btnLogin.isEnabled=true
                viewDialog.hideDialog()
            }
        })
    }

    private fun recoverPassword(email:String){
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.recoverPassword(email)

        call.enqueue(object : Callback<MSG>{
            override fun onResponse(call: Call<MSG>, response: Response<MSG>) {
                viewDialog.hideDialog()
                val resSuccess = response!!.body()!!.success
                val resMsg = response.body()!!.message
                if (resSuccess == 1){
                    showInfoDialog()
                } else {
                    Toast.makeText(applicationContext,"کاربر موجود نیست!",Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<MSG>, t: Throwable) {
                viewDialog.hideDialog()
                Toast.makeText(applicationContext,R.string.network_failure_try,Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showInfoDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_info)
        dialog.setCancelable(true)

        val tvTitle:TextView = dialog.findViewById(R.id.title)
        val tvContent:TextView = dialog.findViewById(R.id.tvInfoContent)

        tvTitle.textSize = 14f
        tvContent.textSize = 12f
        tvContent.gravity = Gravity.RIGHT

        tvTitle.text = "رمز عبور به ایمیل شما ارسال شد"
        tvContent.text = "اگر ایمیل را در inbox پیدا نکردید قسمت spam را چک کنید"

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT




        dialog.findViewById<AppCompatButton>(R.id.bt_close).setOnClickListener { v ->
            dialog.hide()
        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun showRecoveryDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_recovery)
        dialog.setCancelable(true)


        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT


        val edRecoverEmail:EditText = dialog.findViewById(R.id.edRecoverEmail)
        val tilRecoverEmail:TextInputLayout = dialog.findViewById(R.id.recoverTilEmail)



        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)


        dialog.findViewById<ImageButton>(R.id.bt_close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnRecoverPassword).setOnClickListener {
            val email = edRecoverEmail.text.toString().trim()
            if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                tilRecoverEmail.error = null
                viewDialog.showDialog()
                try {
                    recoverPassword(email)
                } catch (e:Exception){

                }
                dialog.dismiss()
            } else {
                tilRecoverEmail.error = "یک ایمیل معبتر وارد کنید"

            }

        }

        dialog.show()
        dialog.window!!.attributes = lp
    }


    private fun validate(username: String,password: String):Boolean {
        var valid = true

        val pattern: Pattern = Pattern.compile("^[A-Za-z0-9._-]{2,25}\$")

        if (username.isEmpty() || !pattern.matcher(username).matches())
        {
            tilUsername.error = "لطفا یک نام کاربری معتبر وارد کنید"
            valid = false
        } else {
            tilUsername.error = null
        }

        if (password.isEmpty())
        {
            tilPassword.error = "کلمه عبور خالی است"
            valid = false
        } else {
            tilPassword.error = null
        }

        return valid
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId === android.R.id.home) {
            finish()
        } else {
            Toast.makeText(applicationContext, item.title, Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }
}
