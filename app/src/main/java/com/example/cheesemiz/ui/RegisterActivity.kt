package com.example.cheesemiz.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.example.cheesemiz.ParseUtil
import com.example.cheesemiz.R
import com.example.cheesemiz.databinding.ActivityRegisterBinding
import com.example.cheesemiz.ui.model.User
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import java.lang.Exception
import java.nio.file.Files.delete
import java.util.*


class RegisterActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRegisterBinding
    private lateinit var progress: ProgressBar
    private lateinit var uri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_register)
        val parseUtil = ParseUtil()
        binding.apply {
            progress=progressBar
            selectPhotoLabelShow()
            tvLogin.setOnClickListener {
              onBackPressed()
            }
            btnRegister.setOnClickListener {
                if(parseUtil.parseProfilePhoto(profileSelect)) {
                    if (parseUtil.parseUsername(tvUsername) && parseUtil.parseEmail(tvEmail) && parseUtil.parsePassword(tvPassword)) {
                        val email = tvEmail.text.toString().trim()
                        val password = tvPassword.text.toString().trim()
                        registerUser(email, password)
                    }
                }else{
                    Toast.makeText(this@RegisterActivity,"Select profile photo.",Toast.LENGTH_SHORT).show()
                }
            }
            profileSelect.setOnClickListener {
                selectPicture()
            }
        }
        FirebaseInstallations.getInstance().delete()
    }
    private fun parseEmail(tv: TextView):Boolean{
        var emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if(tv.text.toString().trim { it <= ' ' }.matches(emailPattern.toRegex())){
             true
        }else{
            tv.error = "Invalid Email format"
             false
        }

    }
    private fun parsePassword(tv: TextView):Boolean{
        return if(tv.text.toString().trim().length>=6){
            true

        }else{
            tv.error = "Invalid Password"
            false
        }
    }

    private fun registerUser(email: String, password: String){
        load()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loaded()
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    Toast.makeText(
                        this@RegisterActivity,
                        "Successful registration!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                    //Analytics
                    val firebaseAnalytics = Firebase.analytics
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
                        param("email",email)
                        
                    }
                    //Topics
                    if(binding.switchTopic.isChecked){
                        subscribeToTopic()
                    }

                    uploadSelectedProfilePic()
                }else{
                    loaded()
                    Toast.makeText(
                        this@RegisterActivity,
                        task.exception!!.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
    }


    private fun selectPicture() {
        loadImage.launch("image/*")
    }
    private val loadImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                try {
                    binding.profileSelect.setImageURI(it)
                    selectPhotoLabelHide()
                    uri = it!!
                }catch (e:Exception){
                    Toast.makeText(this@RegisterActivity,getString(R.string.toast_no_image_selected),Toast.LENGTH_SHORT).show()
                    selectPhotoLabelShow()
                    Log.d("myTag",e.message.toString())
                }
            })

    private fun uploadSelectedProfilePic(){
        if(uri==null)return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/profile/$filename")
        ref.putFile(uri!!)
                .addOnSuccessListener {
                    Log.d("myTag","successfully uploaded the profile ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener { it ->
                        saveUserToFirebaseDataBase(it.toString())
                    }
                }


    }
    private fun saveUserToFirebaseDataBase(uri:String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(binding.tvUsername.text.toString(),uri,uid)
        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("myTag","Succesfully saved to firebase database")
                    startActivity(
                        Intent(this@RegisterActivity, MainActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                }
    }


    private  fun subscribeToTopic(){
        Firebase.messaging.subscribeToTopic("version_update")
            .addOnCompleteListener { task ->
                var msg  = ""
                if (task.isSuccessful) {
                    msg = getString(R.string.subcriptionTopicSuccess)
                }else{
                    msg = getString(R.string.subcriptionTopicFailed)
                    Log.d("myTag",task.exception.toString())
                }
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectPhotoLabelShow(){
        binding.tvSelectPhoto.visibility=View.VISIBLE
    }
    private fun selectPhotoLabelHide(){
        binding.tvSelectPhoto.visibility=View.GONE
    }

    private fun load(){
        progress.visibility= View.VISIBLE
    }
    private fun loaded(){
        progress.visibility= View.GONE
    }

}