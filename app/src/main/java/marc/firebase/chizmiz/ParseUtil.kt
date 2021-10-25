package marc.firebase.chizmiz

import android.widget.ImageView
import android.widget.TextView

class ParseUtil {
    fun parseEmail(tv: TextView):Boolean{
        var emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if(tv.text.toString().trim { it <= ' ' }.matches(emailPattern.toRegex())){
            true
        }else{
            tv.error = "Invalid Email format"
            false
        }

    }
    fun parsePassword(tv: TextView):Boolean{
        return if(tv.text.toString().trim().length>=6){
            true

        }else{
            tv.error = "Invalid Password"
            false
        }
    }
    fun parseUsername(tv: TextView):Boolean{
        return if(tv.text.toString().trim().length>=3){
            true

        }else{
            tv.error = "Invalid Username"
            false
        }
    }
    fun parseProfilePhoto(im: ImageView):Boolean{
        return im.drawable != null
    }
}