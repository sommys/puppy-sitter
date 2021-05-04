package hu.bme.aut.android.puppysitter

import android.Manifest
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.puppysitter.databinding.ActivityProfileDogBinding
import hu.bme.aut.android.puppysitter.databinding.ActivityProfileSitterBinding
import hu.bme.aut.android.puppysitter.model.Dog
import hu.bme.aut.android.puppysitter.model.User
import hu.bme.aut.android.puppysitter.service.LocationService
import kotlinx.coroutines.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.net.URL
import kotlin.collections.ArrayList

@RuntimePermissions
class ProfileActivity : AppCompatActivity() {
    private lateinit var bindingDog: ActivityProfileDogBinding
    private lateinit var bindingSitter: ActivityProfileSitterBinding
    private lateinit var usrType: String
    private lateinit var usr: User
    private val storageRef = FirebaseStorage.getInstance().reference
    private var locationServiceBinder: LocationService.ServiceLocationBinder? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            locationServiceBinder = binder as LocationService.ServiceLocationBinder
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            locationServiceBinder = null
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usrType = intent.extras?.get("USER_TYPE") as String
        if(usrType == "SITTER"){
            usr = intent.extras?.getParcelable("USER")!!
            bindingSitter = ActivityProfileSitterBinding.inflate(layoutInflater)
            FirebaseFirestore.getInstance().collection("sitters")
                .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                .addOnCompleteListener {
                    loadProfilePicture(it)
                }
            bindingSitter.btnEditProfile.setOnClickListener {
                startActivity(Intent(this, EditProfileActivity::class.java).putExtra("USER_TYPE", "SITTER").putExtra("USER", usr))
            }
            bindingSitter.btnMatch.setOnClickListener {
                startActivity(Intent(this, MatcherActivity::class.java).putExtra("USER_TYPE", "SITTER").putExtra("USER", usr))
            }
        } else {
            usr = intent.extras?.getParcelable<Dog>("USER")!!
            bindingDog = ActivityProfileDogBinding.inflate(layoutInflater)
            setContentView(bindingDog.root)
            FirebaseFirestore.getInstance().collection("dogs")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                    .addOnCompleteListener {
                        loadProfilePicture(it)
                    }
            bindingDog.btnEditProfile.setOnClickListener {
                startActivity(Intent(this, EditProfileActivity::class.java).putExtra("USER_TYPE", "DOG").putExtra("USER", usr))
            }
            bindingDog.btnMatch.setOnClickListener {
                startActivity(Intent(this, MatcherActivity::class.java).putExtra("USER_TYPE", "DOG").putExtra("USER", usr))
            }
        }
    }

    override fun onStart() {
        super.onStart()

        startLocationServiceWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        intent.putExtra("USER_TYPE", usrType)
        intent.putExtra("USER", usr)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        startService(intent)
    }

    override fun onStop() {
        if (locationServiceBinder != null) {
            unbindService(serviceConnection)
            val intent = Intent(this, LocationService::class.java)
            stopService(intent)
        }

        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if(FirebaseAuth.getInstance().currentUser != null) {
            if (usrType == "SITTER") {
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
                                "SITTER" -> {
                                    bindingSitter.ivProfilePicture.setImageBitmap(bitmap)
                                    bindingSitter.profileDetails.tvAge.text = "${usr.age}"
                                    bindingSitter.profileDetails.tvUsername.text = usr.userName
                                    bindingSitter.profileDetails.tvRealName.text = usr.name
                                    bindingSitter.profileDetails.tvBio.text = usr.bio
                                    setContentView(bindingSitter.root)
                                }
                                "DOG" -> {
                                    bindingDog.ivProfilePicture.setImageBitmap(bitmap)
                                    bindingDog.profileDetails.tvAge.text = "${usr.age}"
                                    bindingDog.profileDetails.tvUsername.text = usr.userName
                                    bindingDog.profileDetails.tvRealName.text = usr.name
                                    bindingDog.profileDetails.tvBio.text = usr.bio
                                    bindingDog.profileDetails.tvBreed.text = (usr as Dog).breed
                                    bindingDog.profileDetails.tvWeight.text = "${(usr as Dog).weight}"
                                    bindingDog.profileDetails.tvActivity.text = "${(usr as Dog).activity}"
                                    setContentView(bindingDog.root)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show()
            FirebaseAuth.getInstance().signOut()
            finish()
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