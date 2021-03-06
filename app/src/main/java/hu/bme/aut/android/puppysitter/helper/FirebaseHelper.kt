package hu.bme.aut.android.puppysitter.helper

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import hu.bme.aut.android.puppysitter.ProfileActivity
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.fragment.EditDogFragment
import hu.bme.aut.android.puppysitter.fragment.EditSitterFragment
import hu.bme.aut.android.puppysitter.fragment.UploadPictureDialogFragment
import hu.bme.aut.android.puppysitter.model.Dog
import hu.bme.aut.android.puppysitter.model.User
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class FirebaseHelper {

    companion object {
        private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        private lateinit var firebaseUser: FirebaseUser
        val firebaseStorageRef = FirebaseStorage.getInstance().reference
        private lateinit var firebaseUserStorage: StorageReference
        private val firebaseFirestore = FirebaseFirestore.getInstance()
        fun login(activity: FragmentActivity?, email: String, password: String){
            firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    firebaseUser = firebaseAuth.currentUser!!
                    firebaseUserStorage = firebaseStorageRef.child("images/${firebaseUser.uid}")
                    val intent = Intent(activity, ProfileActivity::class.java)
                    var usrType = ""
                    firebaseFirestore.collection("sitters").document(firebaseUser.uid).get().addOnCompleteListener {
                        if(it.isSuccessful && it.result.contains("email")) {
                            var loc = Location("fused")
                            loc.latitude = (it.result.get("location") as HashMap<String, Any>)["latitude"] as Double
                            loc.longitude = (it.result.get("location") as HashMap<String, Any>)["longitude"] as Double
                            val usr = User(firebaseUser.uid,email, firebaseUser.displayName!!,
                                it.result.get("realName") as String?,
                                it.result.get("pictures") as ArrayList<String>,
                                it.result.get("bio") as String?,
                                it.result.get("age") as Long?,
                                it.result.get("range") as Long?,
                                loc
                            )
                            usrType = it.result?.data?.get("user_type")?.toString() ?: ""
                            if (usrType != "") {
                                intent.putExtra("USER_TYPE", usrType)
                                intent.putExtra("USER", usr)
                                activity?.startActivity(intent)
                                activity?.finish()
                            }
                        } else {
                            firebaseFirestore.collection("dogs").document(firebaseUser.uid).get().addOnCompleteListener { dogtask ->
                                if(dogtask.isSuccessful) {
                                    var loc = Location("fused")
                                    loc.latitude = (dogtask.result.get("location") as HashMap<String, Any>)["latitude"] as Double
                                    loc.longitude = (dogtask.result.get("location") as HashMap<String, Any>)["longitude"] as Double
                                    val usr = Dog(firebaseUser.uid, email, firebaseUser.displayName!!,
                                        dogtask.result.get("realName") as String?,
                                        dogtask.result.get("pictures") as ArrayList<String>,
                                        dogtask.result.get("bio") as String?,
                                        dogtask.result.get("age") as Long?,
                                        dogtask.result.get("range") as Long?,
                                        loc,
                                        dogtask.result.get("breed") as String?,
                                        dogtask.result.get("weight") as Long?,
                                        dogtask.result.get("activity") as Long?
                                    )
                                    usrType = dogtask.result?.data?.get("user_type")?.toString() ?: ""
                                    if(usrType != "") {
                                        intent.putExtra("USER_TYPE", usrType)
                                        intent.putExtra("USER", usr)
                                        activity?.startActivity(intent)
                                        activity?.finish()
                                    }
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, exception.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }

        fun register(activity: FragmentActivity?, email: String, userName: String, realName: String, password: String, usrType: String): Boolean {
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
                    Toast.makeText(activity,"Registration successful", Toast.LENGTH_SHORT).show()
                    if(usrType == "dogs"){
                        val matchType = "sitters"
                        val usr = Dog(firebaseUser.uid, email,userName)
                        var matchablesList: ArrayList<String> = arrayListOf()
                        runBlocking {
                            firebaseFirestore.collection(matchType).get().addOnSuccessListener {qs ->
                                qs.documents.forEach {
                                    matchablesList.add(it.id)
                                }
                                firebaseFirestore.collection("dogs").document(firebaseUser.uid).set(hashMapOf(
                                    "uid" to firebaseUser.uid,
                                    "user_type" to "dogs",
                                    "userName" to usr.userName,
                                    "email" to usr.email,
                                    "realName" to realName,
                                    "pictures" to usr.pictures,
                                    "bio" to usr.bio,
                                    "age" to usr.age,
                                    "range" to 5L,
                                    "location" to usr.location,
                                    "breed" to usr.breed,
                                    "weight" to usr.weight,
                                    "activity" to usr.activity,
                                    "matchables" to matchablesList,
                                    "possibleMatch" to arrayListOf<String>(),
                                    "match" to arrayListOf<String>()
                                ))
                                CoroutineScope(Dispatchers.IO).launch {
                                    addToMatchables(matchType)
                                    uploadDefaultPicture(activity!!, usrType, email, password)
                                }
                            }
                        }
                    } else {
                        val matchType = "dogs"
                        val usr = User(firebaseUser.uid, email,userName)
                        var matchablesList: ArrayList<String> = arrayListOf()
                        runBlocking {
                            firebaseFirestore.collection(matchType).get().addOnSuccessListener {qs ->
                                qs.documents.forEach {
                                    matchablesList.add(it.id)
                                }
                                firebaseFirestore.collection("sitters").document(firebaseUser.uid).set(hashMapOf(
                                    "uid" to firebaseUser.uid,
                                    "user_type" to "sitters",
                                    "userName" to usr.userName,
                                    "realName" to realName,
                                    "email" to usr.email,
                                    "pictures" to usr.pictures,
                                    "bio" to usr.bio,
                                    "age" to usr.age,
                                    "range" to 5L,
                                    "location" to usr.location,
                                    "matchables" to matchablesList,
                                    "possibleMatch" to arrayListOf<String>(),
                                    "match" to arrayListOf<String>()
                                )).addOnSuccessListener {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        addToMatchables(matchType)
                                        uploadDefaultPicture(activity!!, usrType, email, password)
                                    }
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity,exception.localizedMessage, Toast.LENGTH_LONG).show()
                }
            return success
        }

        fun updateLocation(location: Location, usrType: String){
            if(!::firebaseUser.isInitialized){
                firebaseUser = firebaseAuth.currentUser!!
            }
            firebaseFirestore.collection(usrType).document(firebaseUser.uid).update(
                hashMapOf(
                    "location" to location
                ) as Map<String, Any>)
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

        fun removeMatch(m: User, usrType: String){
            firebaseFirestore.collection(usrType).document(firebaseUser.uid).update("match", FieldValue.arrayRemove(m.uid))
        }

        private fun uploadDefaultPicture(activity: FragmentActivity, usrType: String, email: String, password: String,) {
            firebaseUserStorage = firebaseStorageRef.child("images/${firebaseUser.uid}")
            firebaseFirestore.collection(usrType).document(firebaseUser.uid).update("pictures", FieldValue.arrayUnion("images/${firebaseUser.uid}/default_pic"))
            val baos = ByteArrayOutputStream()
            BitmapFactory.decodeStream(activity.resources.openRawResource(R.raw.default_pic)).compress(
                Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            var uploadTask = firebaseUserStorage.child("default_pic").putBytes(data)
            uploadTask.addOnSuccessListener{
                it.storage.downloadUrl.addOnSuccessListener {
                    login(activity, email, password)
                }
            }.addOnFailureListener{}
        }

        suspend fun deletePicture(usr: User, usrType: String, iv: ImageView, resources: Resources, fragment: Fragment){
            try {
                firebaseStorageRef.child("images/${usr.uid}/${iv.contentDescription}").delete().await()
            } catch (e: StorageException){}
            firebaseFirestore.collection(usrType).document(usr.uid!!).update("pictures", FieldValue.arrayRemove("images/${usr.uid}/${iv.contentDescription}")).await()
            withContext(Dispatchers.Main){
                iv.contentDescription = "stock"
                iv.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_plus_circle_black_48dp))
                if(usrType == "dogs"){
                    (fragment as EditDogFragment).rearrangeHolders()
                } else {
                    (fragment as EditSitterFragment).rearrangeHolders()
                }
            }
        }

        suspend fun editPicture(usr: User, usrType: String, iv: ImageView, newImageUri: Uri){
            try {
                firebaseStorageRef.child("images/${usr.uid}/${iv.contentDescription}").delete().await()
                firebaseStorageRef.child("images/${usr.uid}/${newImageUri.toString().substring(newImageUri.toString().lastIndexOf("/")+1)}").putFile(newImageUri).await()
                val idx = usr.pictures.indexOf("images/${usr.uid}/${iv.contentDescription}")
                usr.pictures[idx] = "images/${usr.uid}/${newImageUri.toString().substring(newImageUri.toString().lastIndexOf("/")+1)}"
                firebaseFirestore.collection(usrType).document(usr.uid!!).update("pictures", usr.pictures).await()
            } catch (e: StorageException){}
        }

        suspend fun uploadPicture(usr: User, usrType: String, iv: ImageView, newImageUri: Uri){
            try {
                firebaseStorageRef.child("images/${usr.uid}/${newImageUri.toString().substring(newImageUri.toString().lastIndexOf("/")+1)}").putFile(newImageUri).await()
                usr.pictures.add("images/${usr.uid}/${newImageUri.toString().substring(newImageUri.toString().lastIndexOf("/")+1)}")
                firebaseFirestore.collection(usrType).document(usr.uid!!).update("pictures", FieldValue.arrayUnion("images/${usr.uid}/${newImageUri.toString().substring(newImageUri.toString().lastIndexOf("/")+1)}")).await()
            } catch (e: StorageException){}
        }

        suspend fun editPictureCamera(usr: User, usrType: String, iv: ImageView, img: Bitmap) {
            try {
                val baos = ByteArrayOutputStream()
                img.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                firebaseStorageRef.child("images/${usr.uid}/${iv.contentDescription}").delete().await()
                firebaseStorageRef.child("images/${usr.uid}/${img.hashCode()}").putBytes(data).await()
                val idx = usr.pictures.indexOf("images/${usr.uid}/${iv.contentDescription}")
                usr.pictures[idx] = "images/${usr.uid}/${img.hashCode()}"
                firebaseFirestore.collection(usrType).document(usr.uid!!).update("pictures", usr.pictures).await()
            } catch (e: StorageException){}
            withContext(Dispatchers.Main){
                iv.contentDescription = img.hashCode().toString()
                iv.setImageBitmap(img)
            }
        }

        suspend fun uploadPictureCamera(usr: User, usrType: String, iv: ImageView, img: Bitmap) {
            try {
                val baos = ByteArrayOutputStream()
                img.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                firebaseStorageRef.child("images/${usr.uid}/${img.hashCode()}").putBytes(data).await()
                usr.pictures.add("images/${usr.uid}/${img.hashCode()}")
                firebaseFirestore.collection(usrType).document(usr.uid!!).update("pictures", FieldValue.arrayUnion("images/${usr.uid}/${img.hashCode()}")).await()
            } catch (e: StorageException){}
            withContext(Dispatchers.Main){
                iv.contentDescription = img.hashCode().toString()
                iv.setImageBitmap(img)
            }
        }


        suspend fun initializePictures(activity: FragmentActivity, pictureHolders: ArrayList<ImageView>, usrType: String, usr: User){
            var i = 0
            val it = firebaseFirestore.collection(usrType).document(usr.uid!!).get().await()
            val picturePaths = it["pictures"] as ArrayList<String>
            for (path: String in picturePaths) {
                val picHolder = pictureHolders[i++]
                val uri = firebaseStorageRef.child(path).downloadUrl.await()
                withContext(Dispatchers.Main) {
                    Glide.with(activity).load(uri).into(picHolder)
                    picHolder.contentDescription = path.substring(path.lastIndexOf("/") + 1)
                }
            }
        }

//        suspend fun savePictures(pictureHolders: ArrayList<ImageView>, usrType: String, usr: User){
//            for(i: ImageView in pictureHolders){
//                if(i.contentDescription != "stock" && i.contentDescription != "default_pic"){
//                    val baos = ByteArrayOutputStream()
//                    i.drawable.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos)
//                    val data = baos.toByteArray()
//                    firebaseStorageRef.child("images/${usr.uid}/${i.id}").putBytes(data).await()
//                    i.contentDescription = i.id.toString()
//                } else if (i.contentDescription == "stock"){
//                    try {
//                        firebaseStorageRef.child("images/${firebaseUser.uid}/${i.id}").delete().await()
//                    } catch (e: StorageException){}
//                }
//            }
//        }

        suspend fun saveChanges(usrType: String, usr: User){
            val userData: HashMap<String, Any>
            if(usrType == "dogs"){
                userData = hashMapOf(
                    "bio" to usr.bio,
                    "age" to usr.age,
                    "weight" to (usr as Dog).weight,
                    "activity" to usr.activity,
                    "breed" to usr.breed,
                    "range" to usr.range
                ) as HashMap<String, Any>
            } else {
                userData = hashMapOf(
                    "bio" to usr.bio,
                    "age" to usr.age,
                    "range" to usr.range
                ) as HashMap<String, Any>
            }
            firebaseFirestore.collection(usrType).document(usr.uid!!).update(userData).await()
        }

        suspend fun matchPressed(usrType: String, usr: User, matchType: String, match: User): Boolean{
            firebaseFirestore.collection(usrType).document(usr.uid!!).update("matchables", FieldValue.arrayRemove(match.uid))
            val matchData = firebaseFirestore.collection(matchType).document(match.uid!!).get().await()
            if((matchData["possibleMatch"] as ArrayList<String>).contains(usr.uid)){
                firebaseFirestore.collection(usrType).document(usr.uid!!).update("possibleMatch", FieldValue.arrayRemove(match.uid))
                firebaseFirestore.collection(matchType).document(match.uid!!).update("possibleMatch", FieldValue.arrayRemove(usr.uid))
                firebaseFirestore.collection(matchType).document(match.uid!!).update("match", FieldValue.arrayUnion(usr.uid))
                return true
            } else {
                firebaseFirestore.collection(usrType).document(usr.uid!!).update("possibleMatch", FieldValue.arrayUnion(match.uid))
                firebaseFirestore.collection(matchType).document(match.uid!!).update("possibleMatch", FieldValue.arrayUnion(usr.uid))
                return false
            }
        }

        fun noPressed(usrType: String, usr: User, matchType: String, match: User){
            firebaseFirestore.collection(usrType).document(usr.uid!!).update("matchables", FieldValue.arrayRemove(match.uid))
            firebaseFirestore.collection(usrType).document(usr.uid!!).update("possibleMatch", FieldValue.arrayRemove(match.uid))
            firebaseFirestore.collection(matchType).document(match.uid!!).update("possibleMatch", FieldValue.arrayRemove(usr.uid))
        }
    }
}