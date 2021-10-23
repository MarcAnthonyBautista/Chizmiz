package marc.firebase.chizmiz.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import marc.firebase.chizmiz.R
import marc.firebase.chizmiz.databinding.ActivityMainBinding
import marc.firebase.chizmiz.ui.model.ChatMessage
import marc.firebase.chizmiz.ui.model.User
import marc.firebase.chizmiz.ui.view.LatestMessageRow


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

        }

        binding.apply {
            recentMessageRecycler.adapter = adapter
            recentMessageRecycler.addItemDecoration(DividerItemDecoration(this@MainActivity,DividerItemDecoration.VERTICAL))
        }
        adapter.setOnItemClickListener{ item,view ->

            val row = item as LatestMessageRow

            val intent = Intent(this,ChatLog::class.java)
            intent.putExtra(NewMessageActivity.KEY ,  row.chatPartner)
            startActivity(intent)
        }


        listenForLatestMessages()

    }
    val adapter = GroupAdapter<GroupieViewHolder>()

    val latestMessageMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerView(){
        adapter.clear()
        latestMessageMap.values.forEach{
            adapter.add(LatestMessageRow(it))
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
//                fetchOnlineUserProfile()
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



}