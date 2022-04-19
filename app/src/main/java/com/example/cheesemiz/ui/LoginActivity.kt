package com.example.cheesemiz.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity.apply
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.GravityCompat.apply
import androidx.databinding.DataBindingUtil
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.example.cheesemiz.ParseUtil
import com.google.firebase.messaging.FirebaseMessaging
import marc.firebase.chizmiz.R
import marc.firebase.chizmiz.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var progress: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_login)
        val parseUtil = ParseUtil()
        binding.apply {
            progress=progressBar
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
        }
        FirebaseMessaging.getInstance().setDeliveryMetricsExportToBigQuery(true)
        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        mFirebaseAnalytics.setUserId("123456");


     /*   val mFirebaseAnalytics=FirebaseAnalytics.getInstance(this)
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "product ID")
            putString(FirebaseAnalytics.Param.ITEM_NAME, "product name")
            putDouble(FirebaseAnalytics.Param.DISCOUNT, (3.5))
            putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "item cat")
            putInt(FirebaseAnalytics.Param.PRICE, 1)
            putInt(FirebaseAnalytics.Param.QUANTITY, 1)
        }
        val params = Bundle().apply {
            putParcelableArrayList(FirebaseAnalytics.Param.ITEMS, arrayListOf(bundle))
            putString(FirebaseAnalytics.Param.CURRENCY, "RUB")
            putInt(FirebaseAnalytics.Param.VALUE, 1)
        }
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_CART,params)*/

        /*val firebaseAnalytics = Firebase.analytics
        val itemJeggings = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "product ID")
        }
        val params = Bundle(itemJeggings).apply {
            putLong(FirebaseAnalytics.Param.QUANTITY, 2)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE,params)*/

    }

    //used to test if manual screen tracking affects the automatic screen tracking in the manifest. (no, they will log seperately)
    override fun onResume() {
        super.onResume()
        /*val firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "campaign/list_page")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "campaign/1/detail_page to campaign/9999/detail_page")
        }*/
        val firebaseAnalytics = Firebase.analytics

            var params =  Bundle()
            params.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            params.putDouble(FirebaseAnalytics.Param.VALUE, 20.00)
        firebaseAnalytics.logEvent("test_purchase",params)
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
                       firebaseAnalytics.setUserProperty("param3",email)
                       firebaseAnalytics.setUserProperty("param4",email)
                       firebaseAnalytics.setUserProperty("param5",email)
                       firebaseAnalytics.setUserProperty("param6",email)
                       firebaseAnalytics.setUserProperty("param7",email)
                       firebaseAnalytics.setUserProperty("param8",email)
                       firebaseAnalytics.setUserProperty("param9",email)
                       firebaseAnalytics.setUserProperty("param10",email)
                       firebaseAnalytics.setUserProperty("param11",email)
                       firebaseAnalytics.setUserProperty("param12",email)
                       firebaseAnalytics.setUserProperty("param13",email)
                       firebaseAnalytics.setUserProperty("param14",email)
                       firebaseAnalytics.setUserProperty("param15",email)
                       firebaseAnalytics.setUserProperty("param16",email)
                       firebaseAnalytics.setUserProperty("param17",email)
                       firebaseAnalytics.setUserProperty("param18",email)
                       firebaseAnalytics.setUserProperty("param_hello",email)*/


                    startActivity(Intent(this@LoginActivity, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("user_id",firebaseUser.uid)
                        .putExtra("email_id",email))
                    finish()

                    /*startActivity(Intent(this@LoginActivity, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("user_id",firebaseUser.uid)
                        .putExtra("email_id",email))
                    finish()*/
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


}