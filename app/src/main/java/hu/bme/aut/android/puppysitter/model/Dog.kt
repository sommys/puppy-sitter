package hu.bme.aut.android.puppysitter.model

import android.graphics.Bitmap
import android.location.Location

data class Dog(
    protected val email: String = "",
    protected val userName: String = "",
    protected val name: String = "",
    protected val pictures: MutableList<Bitmap> = mutableListOf<Bitmap>(),
    protected var bio: String = "",
    protected var age: Int = 0,
    protected var location: Location = Location("fused"),
    protected val breed: String = "",
    protected var weight: Int = 0,
    protected var activity: Int = 0
)