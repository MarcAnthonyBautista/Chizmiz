package com.example.cheesemiz.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.cheesemiz.R
import com.example.cheesemiz.RemoteUtil
import com.example.cheesemiz.databinding.ActivityMainBinding
import com.example.cheesemiz.ui.model.ChatMessage
import com.example.cheesemiz.ui.model.User
import com.example.cheesemiz.ui.view.LatestMessageRow
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.BuildConfig
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var user_uid : String
    private lateinit var fireabseConfig : FirebaseRemoteConfig
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    companion object{
        var currentUser: User? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
      //  initFirebaseApp()

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val auth = FirebaseAuth.getInstance()
        val uid = auth.uid

        if(uid==null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }else{
            user_uid=uid
            fetchCurrentUser()
            listenForLatestMessages()
        }
        val ss:String = intent.getStringExtra("data").toString()
        Log.d ("FCM", "data message intent - $ss")
            //do your stuff


        //updateChecker()

        binding.apply {
            recentMessageRecycler.adapter = adapter
            recentMessageRecycler.addItemDecoration(DividerItemDecoration(this@MainActivity,DividerItemDecoration.VERTICAL))
        }
        adapter.setOnItemClickListener{ item,view ->

            val row = item as LatestMessageRow

            val intent = Intent(this, ChatLog::class.java)
            intent.putExtra(NewMessageActivity.KEY,  row.chatPartner)
            startActivity(intent)
        }

    }

    fun initFirebaseApp(){
        val options = FirebaseOptions.Builder()
            .setProjectId("chizmiz-be5ca")
            .setApplicationId("1:849182651066:android:c8b9b53694bac6270a4883")
            .setGcmSenderId("849182651066")
            .build()
        Log.d("initialize APP","here")
        FirebaseApp.initializeApp(this, options, "default")

        Log.d("initial project","current project: ${FirebaseApp.getInstance().options.projectId}")
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    val latestMessageMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerView(){
        adapter.clear()
        latestMessageMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
        //for display purpose only
        //if null
        if(latestMessageMap.isNullOrEmpty()){
            binding.lonelyText.visibility = View.VISIBLE
            binding.arrow.visibility = View.VISIBLE
        }else{
            binding.lonelyText.visibility = View.GONE
            binding.arrow.visibility = View.GONE
        }
    }

    private fun listenForLatestMessages(){
        val fromId = user_uid
        val ref =  FirebaseDatabase.getInstance().getReference("/recent-message/$fromId")
        ref.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
               val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    private fun fetchCurrentUser(){
        val ref = FirebaseDatabase.getInstance().getReference("/users/$user_uid")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               currentUser = snapshot.getValue(User::class.java)
                //this is the original function here
//                fetchOnlineUserProfile()
                //Log the timestamp where the app is active  - this is not the original function here.
                //logTime(currentUser?.username.toString())
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
//    private fun fetchOnlineUserProfile(){
//        FirebaseDatabase.getInstance().getReference("/users").addListenerForSingleValueEvent(object:
//            ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//               val imageUrl = snapshot.child(user_uid).child("profileImageUrl").value
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.d("myTag","error: ${error.message}")
//            }
//
//        })
//    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.nav_new_message -> {
                startActivity(Intent(this, NewMessageActivity::class.java))
            }
            R.id.nav_signout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateChecker(){
        val map:HashMap<String,String> = HashMap()
        map.put(RemoteUtil.VERSION, BuildConfig.VERSION_NAME)

        fireabseConfig = FirebaseRemoteConfig.getInstance()
        var configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(1)
            .build()
        fireabseConfig.setConfigSettingsAsync(configSettings)

        fireabseConfig.setDefaultsAsync(map.toMap())
        fireabseConfig.fetchAndActivate()
            .addOnCompleteListener{
                if(it.isSuccessful){
                    Log.d("TAGG", "Config params updated: ${it.result} - ${it.exception}")
                    Log.d("TAGG","remoteConfig: \n ${fireabseConfig.getString(RemoteUtil.TITLE)} \n ${fireabseConfig.getString(
                        RemoteUtil.WHATSNEW)} \n ${fireabseConfig.getString(RemoteUtil.ISFORCE)} \n ${fireabseConfig.getString(
                        RemoteUtil.VERSION)}")

                    showDialog(fireabseConfig.getString(RemoteUtil.TITLE),fireabseConfig.getString(
                        RemoteUtil.WHATSNEW),fireabseConfig.getLong(RemoteUtil.VERSION),fireabseConfig.getBoolean(
                        RemoteUtil.ISFORCE))
                }
            }
    }

    private fun showDialog(title:String,body:String,version:Long,is_force:Boolean){
        if(version<=Integer.valueOf(BuildConfig.VERSION_NAME))return

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog,null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()
        mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mAlertDialog.setCancelable(false)

        mDialogView.findViewById<TextView>(R.id.dialog_title).text = title
        mDialogView.findViewById<TextView>(R.id.dialog_body).text = body
        if(!is_force){
            mDialogView.findViewById<TextView>(R.id.dialog_negative).apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    mAlertDialog.dismiss()
                }
            }
        }else{
            mDialogView.findViewById<TextView>(R.id.dialog_negative).visibility = View.GONE
        }
    }


   /* override fun onResume(){
        super.onResume()
        val firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Screen_name: MainActivity")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "Screen_class-MainActivity")
        }
    }*/

    /*private fun logTime(email: String){
        //Analytics
        firebaseAnalytics.logEvent("active_time") {
            param("appIsActive_timestamp", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now()))
            param("triggeredBy", email)
            param("sample", FirebaseAnalytics.Param.ITEM_LIST_NAME)

        }
    }*/


}