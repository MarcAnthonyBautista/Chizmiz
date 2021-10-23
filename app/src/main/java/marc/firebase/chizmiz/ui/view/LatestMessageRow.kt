package marc.firebase.chizmiz.ui.view

import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import marc.firebase.chizmiz.R
import marc.firebase.chizmiz.ui.model.ChatMessage
import marc.firebase.chizmiz.ui.model.User

class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){

    var chatPartner : User? = null
    override fun bind(viewHolder: GroupieViewHolder, p1: Int) {

        val chatPartnerId : String = if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatMessage.toId
        }else{
            chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartner = snapshot.getValue(User::class.java)?:return
                viewHolder.itemView.findViewById<TextView>(R.id.recent_message_name).text = chatPartner?.username
                Picasso.get().load(chatPartner?.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(
                    R.id.recent_message_profile))
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        viewHolder.itemView.findViewById<TextView>(R.id.recent_message_item_text).text = chatMessage.text
    }
    override fun getLayout(): Int {
        return R.layout.recent_message_item
    }
}