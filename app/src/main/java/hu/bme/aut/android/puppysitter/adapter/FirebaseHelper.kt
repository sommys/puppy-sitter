package hu.bme.aut.android.puppysitter.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import hu.bme.aut.android.puppysitter.ProfileActivity
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.model.Dog
import hu.bme.aut.android.puppysitter.model.Sitter
import hu.bme.aut.android.puppysitter.model.User
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream

class FirebaseHelper {

    companion object {
        private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        private lateinit var firebaseUser: FirebaseUser
        private val firebaseStorageRef = FirebaseStorage.getInstance().reference
        private lateinit var firebaseUserStorage: StorageReference
        private val firebaseFirestore = FirebaseFirestore.getInstance()
        fun login(activity: FragmentActivity?, email: String, password: String){
            firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    firebaseUser = firebaseAuth.currentUser!!
                    firebaseUserStorage = firebaseStorageRef.child("images/${firebaseUser.uid}")
                    val intent = Intent(activity, ProfileActivity::class.java)

                    //TODO login --> create user object, load data to make it available everywhere
                    //TODO set active user state???
                    var usrType = ""
                    firebaseFirestore.collection("sitters").document(firebaseUser.uid).get().addOnCompleteListener {
                        if(it.isSuccessful){
                            usrType = it.result?.data?.get("user_type")?.toString() ?: ""
                            if(usrType != "") {
                                intent.putExtra("USER_TYPE", usrType)
                                activity?.startActivity(intent)
                                activity?.finish()
                            }
                        }
                    }
                    if(usrType == ""){
                        firebaseFirestore.collection("dogs").document(firebaseUser.uid).get().addOnCompleteListener {
                            if(it.isSuccessful) {
                                usrType = it.result?.data?.get("user_type")?.toString() ?: ""
                                if(usrType != "") {
                                    intent.putExtra("USER_TYPE", usrType)
                                    activity?.startActivity(intent)
                                    activity?.finish()
                                }
                            }
                        }
                    }

                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, exception.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }

        fun register(activity: FragmentActivity?, email: String, userName: String, password: String, usrType: String): Boolean {
            var success = false
            firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val firebaseUser = result.user
                    Companion.firebaseUser = firebaseUser
                    val profileChangeRequest = UserProfileChangeRequest.Builder()
                        .setDisplayName(userName)
                        .build()
                    firebaseUser?.updateProfile(profileChangeRequest)
                    Toast.makeText(activity,"Registration successful",Toast.LENGTH_SHORT).show()
                    if(usrType == "DOG"){
                        val matchType = "sitters"
                        val usr = Dog(email,userName)
                        var matchablesList: ArrayList<String> = arrayListOf()
                        runBlocking {
                            firebaseFirestore.collection(matchType).get().addOnSuccessListener {qs ->
                                qs.documents.forEach {
                                    matchablesList.add(it.id)
                                }
                                Toast.makeText(activity, "Got matchables", Toast.LENGTH_SHORT).show()
                                firebaseFirestore.collection("${usrType.toLowerCase()}s").document(firebaseUser.uid).set(hashMapOf(
                                        "uid" to firebaseUser.uid,
                                        "user_type" to usrType,
                                        "userName" to usr.userName,
                                        "email" to usr.email,
                                        "pictures" to usr.pictures,
                                        "bio" to usr.bio,
                                        "age" to usr.age,
                                        "location" to usr.location,
                                        "matchables" to matchablesList
                                ))
                                CoroutineScope(Dispatchers.IO).launch {
                                    addToMatchables(matchType)
                                    uploadDefaultPicture(activity!!, usrType, email, password)
                                }
                            }
                        }
                    } else {
                        val matchType = "dogs"
                        val usr = Sitter(email,userName)
                        var matchablesList: ArrayList<String> = arrayListOf()
                        runBlocking {
                            firebaseFirestore.collection(matchType).get().addOnSuccessListener {qs ->
                                qs.documents.forEach {
                                    matchablesList.add(it.id)
                                }
                                Toast.makeText(activity, "Got matchables", Toast.LENGTH_SHORT).show()
                                firebaseFirestore.collection("${usrType.toLowerCase()}s").document(firebaseUser.uid).set(hashMapOf(
                                        "uid" to firebaseUser.uid,
                                        "user_type" to usrType,
                                        "userName" to usr.userName,
                                        "email" to usr.email,
                                        "pictures" to usr.pictures,
                                        "bio" to usr.bio,
                                        "age" to usr.age,
                                        "location" to usr.location,
                                        "matchables" to matchablesList
                                ))
                                CoroutineScope(Dispatchers.IO).launch {
                                    addToMatchables(matchType)
                                    uploadDefaultPicture(activity!!, usrType, email, password)
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity,exception.localizedMessage,Toast.LENGTH_LONG).show()
                }
            return success
        }

        private fun addToMatchables(matchType: String){
            firebaseFirestore.collection(matchType).get().addOnSuccessListener {qs ->
                qs.documents.forEach {ds1 ->
                    firebaseFirestore.collection(matchType).document(ds1.id).get().addOnSuccessListener {ds2 ->
                        val matchablesList: ArrayList<String> = ds2.data?.get("matchables") as ArrayList<String>
                        if(!matchablesList.contains(firebaseUser.uid)){
                            matchablesList.add(firebaseUser.uid)
                        }
                        firebaseFirestore.collection(matchType).document(ds2.id).update(hashMapOf(
                                "matchables" to matchablesList
                        ) as Map<String, Any>)
                    }
                }
            }
        }

        private fun uploadDefaultPicture(activity: FragmentActivity, usrType: String, email: String, password: String,) {
            firebaseUserStorage = firebaseStorageRef.child("images/${firebaseUser.uid}")
            val pictures: ArrayList<String> = arrayListOf()
            pictures.add("images/${firebaseUser.uid}/default_pic")
            firebaseFirestore.collection("${usrType.toLowerCase()}s").document(firebaseUser.uid).update(hashMapOf(
                    "pictures" to pictures
            ) as Map<String, Any>)
            val baos = ByteArrayOutputStream()
            BitmapFactory.decodeStream(activity.resources.openRawResource(R.raw.app_logo)).compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            var uploadTask = firebaseUserStorage.child("default_pic").putBytes(data)
            uploadTask.addOnSuccessListener{
                it.storage.downloadUrl.addOnSuccessListener {
                    login(activity, email, password)
                }
            }.addOnFailureListener{}

        }
    }
}