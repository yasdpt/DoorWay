package ir.staryas.doorway.acticities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import ir.staryas.doorway.R
import ir.staryas.doorway.model.MSG
import ir.staryas.doorway.networking.ApiClient
import ir.staryas.doorway.networking.ApiService
import ir.staryas.doorway.utils.ViewDialog
import kotlinx.android.synthetic.main.activity_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {

    companion object {
        val apiClient = ApiClient()
        lateinit var activityTwo:AppCompatActivity

    }

    lateinit var viewDialog: ViewDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        activityTwo = this
        initToolbar()
        initComponent()

    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.signUpToolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title="ثبت نام"
        MainActivity.tools.setSystemBarColor(this,
            R.color.grey_5
        )
        MainActivity.tools.setSystemBarLight(this)
    }

    private fun initComponent(){
        viewDialog = ViewDialog(this)

        btnSignUpLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSignUpGo.setOnClickListener {
            if (isNetworkAvailable()){
                val fullName = edSignUpFullName.text.toString().trim()
                val username = edSignUpUsername.text.toString().trim()
                val email = edSignUpEmail.text.toString().trim()
                val phone = edSignUpPhone.text.toString().trim()
                val password = edSignUpPassword.text.toString().trim()
                val rePassword = edSignUpRePassword.text.toString().trim()

                if (validate(fullName, username, email, password, rePassword, phone)) {
                    btnSignUpGo.isEnabled = false
                    viewDialog.showDialog()
                    SignUp(fullName, username, email, password, phone)
                } else {
                    Toast.makeText(this,"لطفا مقادیر را به درستی وارد کنید",Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this,"لطفا اینترنت خود را متصل و دوباره تلاش کنید",Toast.LENGTH_LONG).show()
            }
        }


    }

    private fun SignUp(fullName:String, username:String, email:String, password:String,phone:String) {
        val service = apiClient.getClient().create(ApiService::class.java)
        val call = service.signUp(fullName,username,email,password,phone)

        call.enqueue(object : Callback<MSG> {
            override fun onResponse(call: Call<MSG>, response: Response<MSG>) {
                if (response.body()!!.success == 1) {
                    viewDialog.hideDialog()
                    Toast.makeText(applicationContext,response.body()!!.message,Toast.LENGTH_LONG).show()
                    if (LoginActivity.activityThree != null)
                    {
                        LoginActivity.activityThree.finish()
                    }
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    viewDialog.hideDialog()
                    Toast.makeText(applicationContext,response.body()!!.message,Toast.LENGTH_LONG).show()
                    btnSignUpGo.isEnabled = true
                }
            }

            override fun onFailure(call: Call<MSG>, t: Throwable) {
                viewDialog.hideDialog()
                btnSignUpGo.isEnabled=true
                Toast.makeText(applicationContext,t.message + " " + t.localizedMessage,Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun validate(fullname:String, username:String, email:String, password:String, rePassword:String,phone:String):Boolean {
        var valid = true

        val usernamePattern: Pattern = Pattern.compile("^[A-Za-z0-9._-]{2,25}\$")
        val phonePattern: Pattern = Pattern.compile("^[+]?[0-9]{10,13}\$")

        if (username.isEmpty() || !usernamePattern.matcher(username).matches())
        {
            signUpTilUsername.error = "نام کاربری معتبر نیست"
            valid = false
        } else {
            signUpTilUsername.error = null
        }

        if(email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signUpTilEmail.error = "ایمیل معتبر نیست"
        } else {
            signUpTilEmail.error = null
        }

        when {
            password.isEmpty() -> {
                signUpTilPassword.error = "کلمه عبور خالی است"
                valid = false
            }
            password.length <= 6 -> {
                signUpTilPassword.error = "کلمه عبور باید بیشتر از ۶ کاراکتر باشد"
                valid = false
            }
            else -> signUpTilPassword.error = null
        }

        if (rePassword.isEmpty())
        {
            signUpTilRePassword.error = "کلمه عبور خالی است"
            valid = false
        } else {
            signUpTilRePassword.error = null
        }

        if (password.length <= 6)
        {
            signUpTilPassword.error = "کلمه عبور باید بیشتر از ۶ کاراکتر باشد"
            valid = false
        } else {
            signUpTilPassword.error = null
        }

        if (password != rePassword){
            Toast.makeText(this,"رمز های عبور مطابق نیستند",Toast.LENGTH_LONG).show()
            valid = false
        } else {

        }

        if (fullname.isEmpty())
        {
            signUpTilFullName.error = "نام خالی است"
            valid = false
        } else {
            signUpTilFullName.error = null
        }

        if (phone.isEmpty() || !phonePattern.matcher(phone).matches()) {
            signUpTilPhone.error = "شماره موبایل نامعتبر است"
            valid = false
        } else {
            signUpTilPhone.error = null
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
        if (item.itemId == android.R.id.home) {
            finish()
        } else {
            Toast.makeText(applicationContext, item.title, Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }



}
