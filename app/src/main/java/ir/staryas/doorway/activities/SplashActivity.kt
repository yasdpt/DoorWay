package ir.staryas.doorway.activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import ir.staryas.doorway.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAY: Long = 2000 //2 seconds

    internal val mRunnable: Runnable = Runnable {
        if (!isFinishing) {

            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Initialize the Handler
        mDelayHandler = Handler()

        if (isNetworkAvailable()){
            //Navigate with delay
            mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY)
        } else {
            animation_view.visibility = View.GONE
            tvSplashNoInternet.visibility = View.VISIBLE
            btnSplashTryAgain.visibility = View.VISIBLE
        }

        btnSplashTryAgain.setOnClickListener {
            if (isNetworkAvailable())
            {
                animation_view.visibility = View.VISIBLE
                tvSplashNoInternet.visibility = View.GONE
                btnSplashTryAgain.visibility = View.GONE
                mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY)
            } else {
                Toast.makeText(this, R.string.no_internet
                    ,Toast.LENGTH_SHORT).show()
            }
        }


    }

    public override fun onDestroy() {

        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }

        super.onDestroy()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }
}
