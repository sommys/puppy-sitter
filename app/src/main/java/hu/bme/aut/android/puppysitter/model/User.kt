package hu.bme.aut.android.puppysitter.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location

data class User(
    val email: String = "",
    val userName: String = "",
//    protected val name: String = "",
//    protected val pictures: MutableList<Bitmap> = mutableListOf<Bitmap>(),
    protected var bio: String = "",
    protected var age: Int = 0,
    protected var location: Location = Location("fused"),
)