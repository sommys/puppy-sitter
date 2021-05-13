package hu.bme.aut.android.puppysitter.extensions

import com.google.android.material.textfield.TextInputEditText

fun TextInputEditText.validateNonEmpty(): Boolean{
    if(text.isNullOrEmpty()){
        error = "Required!"
        return false
    }
    return true
}