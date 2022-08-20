package com.example.cheesemiz.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.cheesemiz.ParseUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import marc.firebase.chizmiz.R
import marc.firebase.chizmiz.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var progress: ProgressBar
    private lateinit var crashButton: Button
    private var TAG = "TAGG"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_login)
        val parseUtil = ParseUtil()
        binding.apply {
            progress=progressBar
            crashButton = btnTestCrash
            tvRegister.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
            btnSignin.setOnClickListener {
                if (parseUtil.parseEmail(tvEmail) && parseUtil.parsePassword(tvPassword)){
                    val email = tvEmail.text.toString().trim()
                    val password = tvPassword.text.toString().trim()
                    signInUser(email,password)
                }
            }

            crashButton.setOnClickListener {
               testCrashlytics()
            }

        }
        FirebaseMessaging.getInstance().setDeliveryMetricsExportToBigQuery(true)
        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        mFirebaseAnalytics.setUserId("123456");

    }

    //used to test if manual screen tracking affects the automatic screen tracking in the manifest. (no, they will log seperately)
    override fun onResume() {
        super.onResume()
        /*val firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "campaign/list_page")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "campaign/1/detail_page to campaign/9999/detail_page")
        }*/
     /*   val firebaseAnalytics = Firebase.analytics

            var params =  Bundle()
            params.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            params.putDouble(FirebaseAnalytics.Param.VALUE, 20.00)
        firebaseAnalytics.logEvent("test_purchase",params)*/

        /////////////////////GET FCM Token
        /*FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = "token: $token"
            Log.d("FCM", msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })*/
    }

    private fun signInUser(email:String,password:String){
        load()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loaded()
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    Toast.makeText(
                        this@LoginActivity,
                        "You are logged in successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    val firebaseAnalytics = Firebase.analytics
                    firebaseAnalytics.setUserProperty("user_email",email)
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
                        param("email",email)
                    }


                /*       firebaseAnalytics.setUserProperty("param1",email)
                       firebaseAnalytics.setUserProperty("param2",email)
                       firebaseAnalytics.setUserProperty("param_hello",email)*/


                    startActivity(Intent(this@LoginActivity, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("user_id",firebaseUser.uid)
                        .putExtra("email_id",email))
                    finish()


                }else{
                    loaded()
                    Toast.makeText(this@LoginActivity,task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }

            }
    }

    private fun load(){
        progress.visibility= View.VISIBLE
    }
    private fun loaded(){
        progress.visibility= View.GONE
    }

    //Test Crashlytics method, called in onCreate in btnTestCrash
    private fun testCrashlytics(){
        crashButton.setOnClickListener {
            throw RuntimeException("Test Crash") // Force a crash
        }
    }


}