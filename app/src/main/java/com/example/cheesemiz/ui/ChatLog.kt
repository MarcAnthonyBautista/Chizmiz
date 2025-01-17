package com.example.cheesemiz.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult
import com.google.mlkit.nl.smartreply.TextMessage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.example.cheesemiz.R
import com.example.cheesemiz.databinding.ActivityChatLogBinding
import com.example.cheesemiz.ui.model.ChatMessage
import com.example.cheesemiz.ui.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.example.cheesemiz.ui.view.ChatFromItem
import com.example.cheesemiz.ui.view.ChatMeItem

class ChatLog : AppCompatActivity() {
    private lateinit var binding : ActivityChatLogBinding
    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser : User? = null
    var whichAdapter : Int = 0
    var conversation = ArrayList<TextMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_chat_log)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.KEY)
        supportActionBar?.title= toUser?.username

        binding.apply {
           //setupDummyData()

            //CHAT INPUT BUTTON
            chatlogInput.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                   // hideKeyboard(v)
                    binding.recyclerSuggest.visibility = View.GONE
                }
            })
            //SEND BUTTONssss
            chatlogSendButton.setOnClickListener{
                if(chatlogInput.text.isNotEmpty()) {
                    binding.recyclerSuggest.visibility = View.GONE
                    performSendMessage()
                }
            }

            chatlogRecycler.adapter = adapter


        }

        listenForMessage()
        adapter.setOnItemClickListener{ item,view ->
            if(view.findViewById<TextView>(R.id.chatlog_timestamp).visibility==View.VISIBLE) {
                view.findViewById<TextView>(R.id.chatlog_timestamp).visibility=View.GONE
            }else{
                view.findViewById<TextView>(R.id.chatlog_timestamp).visibility=View.VISIBLE
            }
        }

//        Get the token
      /*  FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
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
                       adapter.add(ChatMeItem(chatMessage.timestamp,chatMessage.text, currentUser))
                   }else {
                       adapter.add(ChatFromItem(chatMessage.timestamp,chatMessage.text,toUser!!))
                       if (toId != null) {
                           addToSmartConversationFromPartner(chatMessage.text,toId)
                       }
                       generateSmartReply()
                   }
                    binding.chatlogRecycler.scrollToPosition(adapter.itemCount-1)
                    binding.chatlogRecycler

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

        val fromId = FirebaseAuth.getInstance().uid
        val toId = intent.getParcelableExtra<User>(NewMessageActivity.KEY)?.uid
        if(fromId==null)return
        if(toId==null)return

        val ref = FirebaseDatabase.getInstance().getReference("/user-message/$fromId/$toId").push()
        val mirrorRef = FirebaseDatabase.getInstance().getReference("/user-message/$toId/$fromId").push()
        val msg = binding.chatlogInput.text.toString()
        val chatText = ChatMessage(ref.key!!,msg,fromId,toId,System.currentTimeMillis()/1000)
        ref.setValue(chatText)
            .addOnSuccessListener {
                Log.d("myTag","saved chat!!! ${ref.key}")
                binding.chatlogInput.text.clear()
                binding.chatlogRecycler.scrollToPosition(adapter.itemCount-1)
                addToSmartConversationFromUser(msg)
            }

        val recentMsgRefMe = FirebaseDatabase.getInstance().getReference("/recent-message/$fromId/$toId")
        recentMsgRefMe.setValue(chatText)

        val recentMsgRefFrom = FirebaseDatabase.getInstance().getReference("/recent-message/$toId/$fromId")
        recentMsgRefFrom.setValue(chatText)




        mirrorRef.setValue(chatText)

    }

    private fun addToSmartConversationFromUser(msg: String){
        conversation.add(TextMessage.createForLocalUser(
            msg, System.currentTimeMillis()))
    }
    private fun addToSmartConversationFromPartner(msg: String, userId : String){
        conversation.add(TextMessage.createForRemoteUser(
            msg, System.currentTimeMillis(), userId))
    }
    private fun generateSmartReply(){
        val smartReplyGenerator = SmartReply.getClient()
        smartReplyGenerator.suggestReplies(conversation)
            .addOnSuccessListener { result ->
                if (result.status == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                   Log.d("myTag","nosuggestion due to language support")
                } else if (result.status == SmartReplySuggestionResult.STATUS_SUCCESS) {
                    val adapter = GroupAdapter<GroupieViewHolder>()
                    binding.recyclerSuggest.visibility = View.VISIBLE
                    //loop the text
                        for (suggestion in result.suggestions) {
                           Log.d("myTag", "suggest : ${suggestion.text}")
                            adapter.add(SuggestItem(suggestion.text))
                        }

                    adapter.setOnItemClickListener{item,view->
                        val item = item as SuggestItem
                        binding.chatlogInput.setText(item.text_item)
                        binding.recyclerSuggest.visibility = View.GONE
                    }
                    //set the adapter
                    binding.recyclerSuggest.adapter = adapter

                }
            }
            .addOnFailureListener {
                Log.d("myTag","exception: ${it.message}")
            }
    }


    class SuggestItem (val text_item:String): Item<GroupieViewHolder>(){
        override fun bind(p0: GroupieViewHolder, p1: Int) {
            p0.itemView.findViewById<TextView>(R.id.suggestion_item).text= text_item
        }

        override fun getLayout(): Int {
            return R.layout.suggestion_item
        }

    }

}