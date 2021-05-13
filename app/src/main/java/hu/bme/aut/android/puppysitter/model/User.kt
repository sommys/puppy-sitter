package hu.bme.aut.android.puppysitter.model

import android.location.Location
import android.os.Parcel
import android.os.Parcelable

open class User(
    val uid: String? = null,
    val email: String? = null,
    val userName: String? = null,
    var name: String? = null,
    var pictures: ArrayList<String> = arrayListOf(),
    var bio: String? = null,
    var age: Long? = null,
    var range: Long? = null,
    var location: Location? = null) : Parcelable {

    constructor(uid: String, email: String, userName: String): this(uid, email, userName, null, arrayListOf(), "", 0, 0, Location("fused"))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(email)
        parcel.writeString(userName)
        parcel.writeString(name)
        parcel.writeStringList(pictures)
        parcel.writeString(bio)
        parcel.writeLong(age?:0L)
        parcel.writeLong(range?:5L)
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
            val uid = parcel.readString()
            val email = parcel.readString()
            val userName = parcel.readString()
            val ret = User(uid, email, userName)
            ret.name = parcel.readString()
            ret.pictures = parcel.createStringArrayList() as ArrayList<String>
            ret.bio = parcel.readString()
            ret.age = parcel.readLong()
            ret.range = parcel.readLong()
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
