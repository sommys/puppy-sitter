package hu.bme.aut.android.puppysitter

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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
    private lateinit var usrType: String
    private val storageRef = FirebaseStorage.getInstance().reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usrType = intent.extras?.get("USER_TYPE") as String
        //TODO get user object from login and set the view items related to the data
        if(usrType == "SITTER"){
            bindingSitter = ActivityProfileSitterBinding.inflate(layoutInflater)
            setContentView(bindingSitter.root)
            FirebaseFirestore.getInstance().collection("sitters")
                .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnCompleteListener {
                    loadProfilePicture(it)
                }
            bindingSitter.btnEditProfile.setOnClickListener {
                startActivity(Intent(this, EditProfileActivity::class.java).putExtra("USER_TYPE", "SITTER"))
            }
            bindingSitter.btnMatch.setOnClickListener {
                startActivity(Intent(this, MatcherActivity::class.java))
            }
        } else {
            bindingDog = ActivityProfileDogBinding.inflate(layoutInflater)
            setContentView(bindingDog.root)
            FirebaseFirestore.getInstance().collection("dogs")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                    .addOnCompleteListener {
                        loadProfilePicture(it)
                    }
            bindingDog.btnEditProfile.setOnClickListener {
                startActivity(Intent(this, EditProfileActivity::class.java).putExtra("USER_TYPE", "DOG"))
            }
            bindingDog.btnMatch.setOnClickListener {
                startActivity(Intent(this, MatcherActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(usrType == "SITTER"){
            FirebaseFirestore.getInstance().collection("sitters")
                .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnCompleteListener {
                    loadProfilePicture(it)
                }
        } else {
            FirebaseFirestore.getInstance().collection("dogs")
                .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnCompleteListener {
                    loadProfilePicture(it)
                }
        }
    }

    private fun loadProfilePicture(it: Task<DocumentSnapshot>){
        if (it.isSuccessful) {
            val picturePaths = it.result?.get("pictures") as ArrayList<String>
            if(picturePaths.size>0) {
                storageRef.child(picturePaths[0]).downloadUrl.addOnSuccessListener { uri ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = getProfilePictureFromURI(uri)

                        withContext(Dispatchers.Main) {
                            when(usrType){
                                "SITTER" -> bindingSitter.ivProfilePicture.setImageBitmap(bitmap)
                                "DOG" -> bindingDog.ivProfilePicture.setImageBitmap(bitmap)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getProfilePictureFromURI(uri: Uri): Bitmap? {
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