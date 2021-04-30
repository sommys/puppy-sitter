package hu.bme.aut.android.puppysitter.adapter

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import hu.bme.aut.android.puppysitter.ProfileActivity

class FirebaseAuthHelper {

    companion object {
        private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        fun login(activity: FragmentActivity?, email: String, password: String): Boolean {
            var success = false
            firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    success = true
                    val intent = Intent(activity, ProfileActivity::class.java)
                    //TODO adatbazisbol megszerezni a tipusat
                    val type: String = if (email.isNullOrEmpty()) "DOG" else "SITTER"
                    intent.putExtra("USER_TYPE", type)
                    activity?.startActivity(intent)
                    activity?.finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, exception.localizedMessage, Toast.LENGTH_LONG).show()
                }
            return success
        }

        fun register(activity: FragmentActivity?, email: String, userName: String, password: String): Boolean {
            var success = false
            firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->

                    val firebaseUser = result.user
                    val profileChangeRequest = UserProfileChangeRequest.Builder()
                        .setDisplayName(userName)
                        .build()
                    firebaseUser?.updateProfile(profileChangeRequest)
                    Toast.makeText(activity,"Registration successful",Toast.LENGTH_SHORT).show()
                    login(activity, email, password)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity,exception.localizedMessage,Toast.LENGTH_LONG).show()
                }
            return success
        }
    }
}