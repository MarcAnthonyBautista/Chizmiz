package marc.firebase.chizmiz.ui.view

import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import marc.firebase.chizmiz.R
import com.example.cheesemiz.ui.model.User
import java.text.SimpleDateFormat

val sdf = SimpleDateFormat("MMM dd hh:mm")
val ampmformat = SimpleDateFormat("HH")
var amOrPmText = "AM"
private lateinit var amOrPm : String
class ChatFromItem(val timestamp: Long, val text:String,val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, p1: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.from_text).text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.from_user_photo))


        val date = sdf.format(timestamp*1000)
        amOrPm = ampmformat.format(timestamp*1000)
        if(amOrPm.toInt()>=12){amOrPmText = "PM" }else{amOrPmText = "AM"}
        viewHolder.itemView.findViewById<TextView>(R.id.chatlog_timestamp).text = "$date $amOrPmText"
    }

    override fun getLayout(): Int {
        return R.layout.chatlog_from_item
    }
}
class ChatMeItem(val timestamp:Long,val text:String,val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, p1: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.me_text).text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.me_user_photo))

        val date = sdf.format(timestamp*1000)
        amOrPm = ampmformat.format(timestamp*1000)
        if(amOrPm.toInt()>=12){amOrPmText = "PM" }else{amOrPmText = "AM"}
        viewHolder.itemView.findViewById<TextView>(R.id.chatlog_timestamp).text = "$date $amOrPmText"
    }

    override fun getLayout(): Int {
        return R.layout.chatlog_me_item
    }

}