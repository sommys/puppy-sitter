package hu.bme.aut.android.puppysitter

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.puppysitter.databinding.ActivityProfileDogBinding
import hu.bme.aut.android.puppysitter.databinding.ActivityProfileSitterBinding
import kotlinx.coroutines.*
import java.io.InputStream
import java.net.URL

class ProfileActivity : AppCompatActivity() {
    private lateinit var bindingDog: ActivityProfileDogBinding
    private lateinit var bindingSitter: ActivityProfileSitterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(intent.extras?.get("USER_TYPE") == "SITTER"){
            bindingSitter = ActivityProfileSitterBinding.inflate(layoutInflater)
            setContentView(bindingSitter.root)
            val storageRef = FirebaseStorage.getInstance().reference
            FirebaseFirestore.getInstance().collection("sitters")
                .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val picturePaths = it.result?.get("pictures") as ArrayList<String>
                        if(picturePaths.size>0) {
                            storageRef.child(picturePaths[0]).downloadUrl.addOnSuccessListener { uri ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    val bitmap = getProfilePicture(uri)

                                    withContext(Dispatchers.Main) {
                                        bindingSitter.ivProfilePicture.setImageBitmap(bitmap)
                                    }
                                }
                            }
                        }
                    }
                }

//            Glide.with(this).load(Uri.parse("https://firebasestorage.googleapis.com/v0/b/puppysitter-627fd.appspot.com/o/images%2Ft8Gbvr1zIQZJzBbfLtSLpkQAggu2%2F2131296471?alt=media&token=86e7dc5c-22b5-4ec5-b875-64d0df8a7754")).into(bindingSitter.ivProfilePicture)
            bindingSitter.btnEditProfile.setOnClickListener {
                startActivity(Intent(this, EditProfileActivity::class.java).putExtra("USER_TYPE", "SITTER"))
            }
            bindingSitter.btnMatch.setOnClickListener {
                startActivity(Intent(this, MatcherActivity::class.java))
            }
        } else {
            bindingDog = ActivityProfileDogBinding.inflate(layoutInflater)
            setContentView(bindingDog.root)
            val storageRef = FirebaseStorage.getInstance().reference
            FirebaseFirestore.getInstance().collection("dogs")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val picturePaths = it.result?.get("pictures") as ArrayList<String>
                            if(picturePaths.size>0) {
                                storageRef.child(picturePaths[0]).downloadUrl.addOnSuccessListener { uri ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val bitmap = getProfilePicture(uri)

                                        withContext(Dispatchers.Main) {
                                            bindingDog.ivProfilePicture.setImageBitmap(bitmap)
                                        }
                                    }
                                }
                            }
                        }
                    }
            bindingDog.btnEditProfile.setOnClickListener {
                startActivity(Intent(this, EditProfileActivity::class.java).putExtra("USER_TYPE", "DOG"))
            }
        }
    }

    private fun getProfilePicture(uri: Uri): Bitmap? {
        val url = URL(uri.toString())
        return BitmapFactory.decodeStream(url.openConnection().getInputStream())
    }

    override fun onBackPressed() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, StartActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}