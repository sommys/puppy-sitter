package hu.bme.aut.android.puppysitter.model

import android.location.Location
import android.os.Parcel
import android.os.Parcelable

class Dog(
    uid: String?, email:String?, userName: String?, name: String?, pictures: ArrayList<String>, bio: String?, age: Long?, range: Long?, location: Location?,
    var breed: String? = null,
    var weight: Long? = null,
    var activity: Long? = null
): User(uid, email, userName, name, pictures, bio, age, range, location), Parcelable{
    constructor(uid: String, email: String, userName: String): this(uid, email, userName,null, arrayListOf(),"",0, 0, Location("fused"),null,0,0){
        location?.latitude = 0.0
        location?.longitude = 0.0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
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
        parcel.writeString(breed)
        if(weight == null){
            parcel.writeLong(0L)
        } else {
            parcel.writeLong(weight!!)
        }
        if(activity == null){
            parcel.writeLong(0L)
        } else {
            parcel.writeLong(activity!!)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): Dog {
            val ret = Dog(parcel.readString()?:"",parcel.readString()?:"", parcel.readString()?:"")
            ret.name = parcel.readString()
            ret.pictures = parcel.createStringArrayList() as ArrayList<String>
            ret.bio = parcel.readString()
            ret.age = parcel.readLong()
            var newLocation = Location("fused")
            newLocation.latitude = parcel.readDouble()
            newLocation.longitude = parcel.readDouble()
            ret.location = newLocation
            ret.breed = parcel.readString()
            ret.weight = parcel.readLong()
            ret.activity = parcel.readLong()
            return ret
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}