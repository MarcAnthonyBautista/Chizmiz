package marc.firebase.chizmiz.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (
    val username:String,
    val profileImageUrl: String,
    val uid: String
        ):Parcelable{
    constructor():this("","","")

}