package hu.bme.aut.android.puppysitter.model

import android.graphics.Bitmap
import android.location.Location

data class Sitter(
    val email: String? = null,
    val userName: String? = null,
//    protected val name: String = "",
//    protected val pictures: MutableList<Bitmap> = mutableListOf<Bitmap>(),
    val bio: String? = null,
    val age: Int? = null,
    val location: Location? = null
)
