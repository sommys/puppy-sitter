package hu.bme.aut.android.puppysitter.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.puppysitter.databinding.FragmentLoadingBinding
import hu.bme.aut.android.puppysitter.model.Dog
import hu.bme.aut.android.puppysitter.model.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URL

class LoadingProfileFragment: Fragment() {
    private lateinit var binding: FragmentLoadingBinding
    private lateinit var usrType: String
    private lateinit var navController: NavController
    private val user = FirebaseAuth.getInstance().currentUser
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        usrType = activity?.intent?.extras?.get("USER_TYPE") as String
        binding = FragmentLoadingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        GlobalScope.launch { loginProfile() }
    }

    suspend fun loginProfile(){
        val userData = firestore.collection(usrType).document(user.uid).get().await()
        val usr: User
        val action: NavDirections
        val awayMatches = arrayListOf<User>()
        val loc = Location("fused")
        loc.latitude = (userData["location"] as HashMap<*, *>)["latitude"] as Double
        loc.longitude = (userData["location"] as HashMap<*, *>)["longitude"] as Double
        if(usrType == "dogs"){
            usr = Dog(userData["uid"] as String, userData["email"] as String, userData["userName"] as String,
                    userData["realName"] as String?,
                    userData["pictures"] as ArrayList<String>,
                    userData["bio"] as String?,
                    userData["age"] as Long?,
                    userData["range"] as Long?,
                    loc,
                    userData["breed"] as String?,
                    userData["weight"] as Long?,
                    userData["activity"] as Long?)
            if((userData["match"] as ArrayList<String>).isNotEmpty()){
                for(uid: String in userData["match"] as ArrayList<String>){
                    val matchData = firestore.collection("sitters").document(uid).get().await()
                    awayMatches.add(User(uid, matchData["email"] as String, matchData["userName"] as String))
                }
            }
            val uri = storage.child(usr.pictures[0]).downloadUrl.await()
//            val picture = getProfilePictureFromURI(uri)
            action = LoadingProfileFragmentDirections.actionLoadingProfileFragmentToProfileDogFragment(usr, uri.toString(), awayMatches.toTypedArray())
        } else {
            usr = User(userData["uid"] as String, userData["email"] as String, userData["userName"] as String,
                    userData["realName"] as String?,
                    userData["pictures"] as ArrayList<String>,
                    userData["bio"] as String?,
                    userData["age"] as Long?,
                    userData["range"] as Long?,
                    loc)
            if((userData["match"] as ArrayList<String>).isNotEmpty()){
                for(uid: String in userData["match"] as ArrayList<String>){
                    val matchData = firestore.collection("dogs").document(uid).get().await()
                    awayMatches.add(User(uid, matchData["email"] as String, matchData["userName"] as String))
                }
            }
            val uri = storage.child(usr.pictures[0]).downloadUrl.await()
//            val picture = getProfilePictureFromURI(uri)
            action = LoadingProfileFragmentDirections.actionLoadingProfileFragmentToProfileSitterFragment(usr, uri.toString(), awayMatches.toTypedArray())
        }
        activity?.runOnUiThread { navController.navigate(action) }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getProfilePictureFromURI(uri: Uri): Bitmap{
        val url = URL(uri.toString())
        Log.d("IMGURL", url.toString())
        var retBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
        val exif = ExifInterface(uri.path!!)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
        if(orientation == ExifInterface.ORIENTATION_ROTATE_90){
            val matrix = Matrix()
            matrix.postRotate(90F)
            retBitmap = Bitmap.createBitmap(retBitmap, 0, 0, retBitmap.getWidth(), retBitmap.getHeight(), matrix, true)
        } else if(orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            val matrix = Matrix()
            matrix.postRotate(-90F)
            retBitmap = Bitmap.createBitmap(retBitmap, 0, 0, retBitmap.getWidth(), retBitmap.getHeight(), matrix, true)
        }
        return retBitmap
    }
}