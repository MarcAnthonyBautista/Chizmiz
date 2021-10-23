package marc.firebase.chizmiz.ui.view

import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import marc.firebase.chizmiz.R
import marc.firebase.chizmiz.ui.model.User

class ChatFromItem(val text:String,val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, p1: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.from_text).text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.from_user_photo))
    }

    override fun getLayout(): Int {
        return R.layout.chatlog_from_item
    }
}
class ChatMeItem(val text:String,val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, p1: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.me_text).text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.me_user_photo))
    }

    override fun getLayout(): Int {
        return R.layout.chatlog_me_item
    }

}