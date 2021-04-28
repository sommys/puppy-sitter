package hu.bme.aut.android.puppysitter.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location

abstract class User() {
    protected val userName: String = ""
    protected val name: String = ""
    protected val pictures: MutableList<Bitmap> = TODO()
    protected var bio: String = ""
    protected var age: Int = 0
    protected var location: Location = Location("fused")
}