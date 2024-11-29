package com.example.cheesemiz.ui

//import com.google.firebase.remoteconfig.FirebaseRemoteConfig
//import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.cheesemiz.ParseUtil
import com.example.cheesemiz.R
import com.example.cheesemiz.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.inappmessaging.inAppMessaging
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging.getInstance
import com.google.firebase.messaging.messaging
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings


class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var progress: ProgressBar
    private lateinit var crashButton: Button
    private var TAG = "FirebaseTAGS"
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var firebaseAnalytics: FirebaseAnalytics

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
            firebaseAnalytics = Firebase.analytics
            crashButton.setOnClickListener {

             /*   firebaseAnalytics.logEvent(FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY) {
                    param(FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME,"eToken")
                    param(FirebaseAnalytics.Param.VALUE, 2.99)
                }*/
               testCrashlytics()
               /* val firebaseAnalytics = Firebase.analytics
                for (i in 1..2) {
                    firebaseAnalytics.logEvent(
                        FirebaseAnalytics.Event.LOGIN,null)
                }
*/

               /* Firebase.messaging.subscribeToTopic("banana")
                    .addOnCompleteListener { task ->
                        var msg  = ""
                        if (task.isSuccessful) {
                            msg = getString(R.string.subcriptionTopicSuccess)
                        }else{
                            msg = getString(R.string.subcriptionTopicFailed)
                            Log.d("myTag",task.exception.toString())
                        }
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                }*/
            }

        }

        getInstance().setDeliveryMetricsExportToBigQuery(true)

        val mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        mFirebaseAnalytics.setUserId("123456")

        initRCSettings()

        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate : ConfigUpdate) {
                Log.d(TAG, "Updated keys: " + configUpdate.updatedKeys)

                if (configUpdate.updatedKeys.contains("label_sign_in")) {
                    remoteConfig.activate().addOnCompleteListener {
                       downloadRCUpdates()
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w(TAG, "Config update error with code: " + error.code, error)
            }
        })

        setUserProperty()


        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        FirebaseInstallations.getInstance().getToken(false)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Installations", "Installation auth token: " + task.result?.token)
                } else {
                    Log.e("Installations", "Unable to get Installation auth token")
                }
            }
        subscribeToTopic()
        logSomeAnalyticsEvents()

    }

    private fun trackScreen(){
        Log.d("test-screen","insideTrackScreenMethod")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Smart Life>사운드 갤러리>전시 상세 페이지>뻑-온앤오프>작품 듣기")
            //param
        // (FirebaseAnalytics.Param.SCREEN_CLASS, "LoginActivityManual")
        }
    }

    private  fun subscribeToTopic(){
        Log.d("myTag","subscription method")
        Firebase.messaging.subscribeToTopic("version_update")
            .addOnCompleteListener { task ->
                var msg  = ""
                if (task.isSuccessful) {
                    msg = getString(R.string.subcriptionTopicSuccess)
                    Log.d("myTag","successfully subscribed to topics")
                }else{
                    msg = getString(R.string.subcriptionTopicFailed)
                    Log.d("myTag",task.exception.toString())
                }
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
    }

    //used to test if manual screen tracking affects the automatic screen tracking in the manifest. (no, they will log seperately)
    override fun onResume() {

        //trackScreen()
        Log.d("test-screen","Test2")
        super.onResume()
        updateUI()
        Firebase.inAppMessaging.triggerEvent("firstTrigger")
  //      ==============Custom suffix
        /*val suffix = textEdit.getText()
        setLink(Uri.parse("https://www.example.com/$suffix"))*/




       // setUserProperty()
        /* val firebaseAnalytics = Firebase.analytics
              firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
             param(FirebaseAnalytics.Param.SCREEN_NAME, "campaign/list_page")
             param(FirebaseAnalytics.Param.SCREEN_CLASS, "campaign/1/detail_page to campaign/9999/detail_page")
         }*/
        //Log event with null params
//        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW,null) {
//        }


        val params = Bundle()
        val item1 = Bundle()
        item1.putLong(FirebaseAnalytics.Param.QUANTITY, 1)
        item1.putString(FirebaseAnalytics.Param.ITEM_BRAND, "Nike")
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, "itemName")
       // params.putParcelable("user", item1)

            params.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            params.putDouble(FirebaseAnalytics.Param.VALUE, 20.00)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_CART,params)

        val firstItemInCart = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "CSTM_123")
            putString(FirebaseAnalytics.Param.ITEM_NAME, "jeggings")
            putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "pants")
        }

        val secondItemInCart = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "CSTM_456")
            putString(FirebaseAnalytics.Param.ITEM_NAME, "Socks")
            putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "bottoms")
        }



        val finalCart = Bundle()


        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
           // param(FirebaseAnalytics.Param.ITEMS, itemFirstCart)
            param(FirebaseAnalytics.Param.ITEMS, arrayOf(firstItemInCart,secondItemInCart));
        }


        // TODO *DONE* GET FCM Token
        getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = "token: $token"
            Log.d(TAG, "FCM token: $msg")
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
        Log.d("current project:","current project: ${FirebaseApp.getInstance().options.projectId}")
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

                    val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
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

          /*  val options = FirebaseOptions.Builder()
                .setProjectId("new-recipebook-4057e")
                .setApplicationId("1:251023880686:android:10211d6592933f2d999eff")
                .setGcmSenderId("251023880686")
                .build()*/


// Get the database for the other app.
         //   Log.d("switching success","current project: ${FirebaseApp.getInstance("secondary").options.projectId}")
//log FCM Token
         /*   Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                // Log and toast
                val msg = "project 2: $token"
                Log.d(TAG, "FCM token: $msg")
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })*/

            /*
            val firebaseAnalytics = Firebase.analytics

            val mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
            mFirebaseAnalytics.setUserId(null)

            firebaseAnalytics.setUserProperty("apple","iphone")
            firebaseAnalytics.logEvent("eventAfterUserPropertyChange",null)
            //  throw RuntimeException("Test Crash") // Force a crash

            //user prop
        /*  val firebaseAnalytics = Firebase.analytics
            firebaseAnalytics.setUserProperty("sampleUserProperty","sample")
        */

            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, "overide screenName")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "override screenClass")
            }

            // event
            firebaseAnalytics.logEvent("exception") {
                param("description","error 143")
            }
            // Event to dup description:
            firebaseAnalytics.logEvent("response") {
                param("description","message sent")
            }






           //TODO set the consent here then try to get the sessionID...
            //test getSessionID
            firebaseAnalytics.logEvent("sessionIDEvent"){
                param("sample_event_param","sample_value")
                param(FirebaseAnalytics.Param.SCREEN_CLASS, "OverrideScreen-Login")
            }
            firebaseAnalytics.sessionId.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "sessionID: ${task.result}")
                    Toast.makeText(applicationContext,"sessionID: ${task.result}",Toast.LENGTH_LONG ).show()

                } else {
                    Toast.makeText(this, "Session ID fetch failed",
                        Toast.LENGTH_SHORT).show()
                }
            }
            firebaseAnalytics.appInstanceId.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "app Instance ID: ${task.result}")

                } else {
                    Toast.makeText(this, "Instance ID fetch failed",
                        Toast.LENGTH_SHORT).show()
                }
            }

            //test get the token

            //Test purchase



          }
        }

        */

            //Test discount
           val params = Bundle()
            val item1 = Bundle()
            item1.putDouble(FirebaseAnalytics.Param.DISCOUNT, 12.5)
            item1.putDouble(FirebaseAnalytics.Param.PRICE, 13.00)
            item1.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Pants")
            params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, arrayOf(item1))

            // params.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            // params.putDouble(FirebaseAnalytics.Param.VALUE, 20.00)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM,params)




            // params.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            // params.putDouble(FirebaseAnalytics.Param.VALUE, 20.00)
            /*firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM){
                param(FirebaseAnalytics.Param.DISCOUNT, 10.22)
            }*/
            trackScreen()

            //test FIAM event trigger
            Firebase.inAppMessaging.triggerEvent("secondTrigger")
            // end of FIAM event trigger test

        }
    }


    private fun initRCSettings(){
        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 1
        }
            remoteConfig.setConfigSettingsAsync(configSettings)

        downloadRCUpdates()
    }

    private fun downloadRCUpdates(){

       // remoteConfig.setDefaultsAsync(R.xml.rc_login_default_values)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result

                    Log.d(TAG, "Config params updated: $updated - Login")
                    Log.d("TAGG","remoteConfig ${remoteConfig.getString(
                        "label_sign_in")}")

                    Toast.makeText(this, "Fetch and activate succeeded",
                        Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this, "Fetch failed",
                        Toast.LENGTH_SHORT).show()
                }
            }

        val map = remoteConfig.all.mapValues { it.value.asString() }
        println("RC--$map")



    }

    private fun updateUI(){
        remoteConfig.activate()
        findViewById<TextView>(R.id.tv_register).text = remoteConfig.getString("label_register")
        findViewById<TextView>(R.id.btn_signin).text = remoteConfig.getString("label_sign_in")
        findViewById<TextView>(R.id.textView).text = remoteConfig.getString("label_no_account")
    }

    private fun setUserProperty(){
        firebaseAnalytics.setUserProperty("isVegetarian","No")
        //firebaseAnalytics.setUserId("cheese01704")

        //firebaseAnalytics.setUserProperty("someId","009988776655")
//        for (value in 1..26){
//            firebaseAnalytics.setUserProperty("userPropertyy$value",value.toString())
//        }

//        firebaseAnalytics.logEvent("eventBeforeUserPropertyChange",null)
    }

    private fun logSomeAnalyticsEvents(){
        //var itemsArray: Parcelable = [{"item_id":"1511","item_name":"Mandarin Red","price":0,"quantity":1,"item_product_type":"","item_finish":"60 Fine Pearl","item_size":"14,8 x 21 cm","item_designgroup":"Plain Colours"}]

        val itemsArray = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, "1511")
            putString(FirebaseAnalytics.Param.ITEM_NAME, "Mandarin Red")
            putDouble(FirebaseAnalytics.Param.PRICE, 0.0)
            putString(FirebaseAnalytics.Param.QUANTITY, "1")
            putString("item_product_type", "")
            putString("item_finish", "60 Fine Pearl")
            putString("item_size", "14,8 x 21 cm")
            putString("item_designgroup", "Plain Colours")

        }

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE){
            param(FirebaseAnalytics.Param.TRANSACTION_ID, "T12345")
            param(FirebaseAnalytics.Param.CURRENCY, "USD")
            param(FirebaseAnalytics.Param.VALUE, 41.0)
            param(FirebaseAnalytics.Param.TAX, 0)
            param(FirebaseAnalytics.Param.SHIPPING, 0)
            param(FirebaseAnalytics.Param.COUPON, "")
            param(FirebaseAnalytics.Param.ITEMS, arrayOf(itemsArray))
        }
        firebaseAnalytics.logEvent("screen_view") {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "LoginActivityScreenz")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "LoginActivityScreenz")
        }
    }
}