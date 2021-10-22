package marc.firebase.chizmiz.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import marc.firebase.chizmiz.R
import marc.firebase.chizmiz.databinding.ActivityMainBinding
import marc.firebase.chizmiz.ui.model.User


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var user_uid : String

    companion object{
        var currentUser: User? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val auth = FirebaseAuth.getInstance()
        val uid = auth.uid


        if(uid==null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }else{
            user_uid=uid
            fetchCurrentUser()
            fetchOnlineUserProfile()
        }

    }
    private fun fetchCurrentUser(){
        val ref = FirebaseDatabase.getInstance().getReference("/users/$user_uid")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               currentUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun fetchOnlineUserProfile(){
        FirebaseDatabase.getInstance().getReference("/users").addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               val imageUrl = snapshot.child(user_uid).child("profileImageUrl").value
                Picasso.get().load(imageUrl.toString()).into(binding.btnProfile)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("myTag","error: ${error.message}")
            }

        })
    }
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



}