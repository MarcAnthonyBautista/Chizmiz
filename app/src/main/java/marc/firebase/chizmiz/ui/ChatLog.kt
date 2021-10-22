package marc.firebase.chizmiz.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import marc.firebase.chizmiz.R
import marc.firebase.chizmiz.databinding.ActivityChatLogBinding
import marc.firebase.chizmiz.ui.model.ChatMessage
import marc.firebase.chizmiz.ui.model.User

class ChatLog : AppCompatActivity() {
    private lateinit var binding : ActivityChatLogBinding
    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser : User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_chat_log)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.KEY)
        supportActionBar?.title= toUser?.username


        binding.apply {
           //setupDummyData()
            chatlogSendButton.setOnClickListener{
                if(chatlogInput.text.isNotEmpty()) {
                    performSendMessage()
                }
            }

            chatlogRecycler.adapter = adapter
        }
        listenForMessage()
    }
    private fun listenForMessage(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref= FirebaseDatabase.getInstance().getReference("/user-message/$fromId/$toId")
        ref.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
               val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if(chatMessage!=null) {
                   if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                       val currentUser = MainActivity.currentUser ?:return
                       adapter.add(ChatMeItem(chatMessage.text, currentUser))
                   }else {
                       adapter.add(ChatFromItem(chatMessage.text,toUser!!))
                   }
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun performSendMessage(){
        //val ref = FirebaseDatabase.getInstance().getReference("/message").push()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = intent.getParcelableExtra<User>(NewMessageActivity.KEY)?.uid
        if(fromId==null)return
        if(toId==null)return

        val ref = FirebaseDatabase.getInstance().getReference("/user-message/$fromId/$toId").push()
        val mirrorRef = FirebaseDatabase.getInstance().getReference("/user-message/$toId/$fromId").push()

        val chatText = ChatMessage(ref.key!!,binding.chatlogInput.text.toString(),fromId,toId,System.currentTimeMillis()/1000)
        ref.setValue(chatText)
            .addOnSuccessListener {
                Log.d("myTag","saved chat!!! ${ref.key}")
                binding.chatlogInput.text.clear()
                binding.chatlogRecycler.scrollToPosition(adapter.itemCount-1)
            }
        mirrorRef.setValue(chatText)

    }

    class ChatFromItem(val text:String,val user:User): Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, p1: Int) {
                viewHolder.itemView.findViewById<TextView>(R.id.from_text).text = text
                Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.from_user_photo))
        }

        override fun getLayout(): Int {
            return R.layout.chatlog_from_item
        }
    }
    class ChatMeItem(val text:String,val user:User): Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, p1: Int) {
                viewHolder.itemView.findViewById<TextView>(R.id.me_text).text = text
            Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.me_user_photo))
        }

        override fun getLayout(): Int {
            return R.layout.chatlog_me_item
        }

    }

}