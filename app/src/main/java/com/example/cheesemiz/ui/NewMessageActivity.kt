package com.example.cheesemiz.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import marc.firebase.chizmiz.R
import marc.firebase.chizmiz.databinding.ActivityNewMessageBinding
import com.example.cheesemiz.ui.model.User

class NewMessageActivity : AppCompatActivity() {
    private lateinit var binding : ActivityNewMessageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this,R.layout.activity_new_message)
        supportActionBar?.title= getString(R.string.actionbar_select_user)

        fetchUsers()
    }
    companion object{
        val KEY = "_userprofile"
    }
    private fun fetchUsers(){
        FirebaseDatabase.getInstance().getReference("/users").addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach{
                    val user= it.getValue(User::class.java)
                    if(user!=null){
                        if(MainActivity.currentUser?.uid!=user.uid) {
                            adapter.add(UserItem(user))
                        }
                    }
                }
                adapter.setOnItemClickListener{item,view->
                    val userItem = item as UserItem
                    startActivity(Intent(view.context, ChatLog::class.java).putExtra(KEY,userItem.user))
                    finish()
                }
                binding.newMessageRecycler.adapter = adapter

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("myTag","Error: ${error.message}")
            }

        }
        )

    }
    class UserItem(val user: User): Item<GroupieViewHolder>(){
        override fun bind(p0: GroupieViewHolder, p1: Int) {
                p0.itemView.findViewById<TextView>(R.id.item_username).text = user.username
                //p0.itemView.findViewById<ImageView>(R.id.userPhoto).setImageURI(user.profileImageUrl.toUri())
            Picasso.get().load(user.profileImageUrl).into(p0.itemView.findViewById<ImageView>(R.id.userPhoto))
        }

        override fun getLayout(): Int {
            return R.layout.each_user_layout
        }

    }
}