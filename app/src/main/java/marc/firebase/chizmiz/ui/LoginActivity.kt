package marc.firebase.chizmiz.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import marc.firebase.chizmiz.R
import marc.firebase.chizmiz.RemoteUtil
import marc.firebase.chizmiz.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.apply {
            progress=progressBar
            tvRegister.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
            btnSignin.setOnClickListener {
                if (parseEmail(tvEmail) && parsePassword(tvPassword)){
                    val email = tvEmail.text.toString().trim()
                    val password = tvPassword.text.toString().trim()
                    signInUser(email,password)
                }
            }
        }

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


}