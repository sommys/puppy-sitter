package hu.bme.aut.android.puppysitter.model

import android.location.Location
import android.os.Parcel
import android.os.Parcelable

open class User(
    val email: String? = null,
    val userName: String? = null,
    var name: String? = null,
    var pictures: ArrayList<String> = arrayListOf(),
    var bio: String? = null,
    var age: Long? = null,
    var location: Location? = null) : Parcelable {

    constructor(email: String, userName: String): this(email,userName,null, arrayListOf(),"",0,Location("fused"))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(email)
        parcel.writeString(userName)
        parcel.writeString(name)
        parcel.writeStringList(pictures)
        parcel.writeString(bio)
        parcel.writeLong(age?:0L)
        if(location == null){
            parcel.writeDouble(0.0)
            parcel.writeDouble(0.0)
        } else {
            parcel.writeDouble(location!!.latitude)
            parcel.writeDouble(location!!.longitude)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            val email = parcel.readString()
            val userName = parcel.readString()
            val ret = User(email, userName)
            ret.name = parcel.readString()
            ret.pictures = parcel.createStringArrayList() as ArrayList<String>
            ret.bio = parcel.readString()
            ret.age = parcel.readLong()
            var newLocation = Location("fused")
            newLocation.latitude = parcel.readDouble()
            newLocation.longitude = parcel.readDouble()
            ret.location = newLocation
            return ret
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
