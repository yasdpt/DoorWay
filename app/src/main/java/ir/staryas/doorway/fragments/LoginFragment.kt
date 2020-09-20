package ir.staryas.doorway.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ir.staryas.doorway.R
import ir.staryas.doorway.activities.LoginActivity
import kotlinx.android.synthetic.main.fragment_login.view.*




class LoginFragment : Fragment() {

    companion object {

        @JvmStatic
        fun newInstance() =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    // putString(ARG_PARAM1, param1)
                }
            }
    }

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

        val view = inflater.inflate(R.layout.fragment_login, container, false)

        view.btnLoginLogin.setOnClickListener {
            val intent = Intent(view.context, LoginActivity::class.java)
            startActivity(intent)

            // Clear data
            /*val intent = activity!!.intent
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_NO_ANIMATION)
            activity!!.overridePendingTransition(0, 0)
            activity!!.finish()

            activity!!.overridePendingTransition(0, 0)
            startActivity(intent)*/
        }


        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Initialize UI
    }


}