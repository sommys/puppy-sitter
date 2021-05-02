package hu.bme.aut.android.puppysitter.model

import android.graphics.Bitmap
import android.location.Location

data class Dog(
    val email: String = "",
    val userName: String = "",
    val name: String = "",
    val pictures: MutableList<Bitmap> = mutableListOf<Bitmap>(),
    var bio: String = "",
    var age: Int = 0,
    var location: Location = Location("fused"),
    val breed: String = "",
    var weight: Int = 0,
    var activity: Int = 0
)