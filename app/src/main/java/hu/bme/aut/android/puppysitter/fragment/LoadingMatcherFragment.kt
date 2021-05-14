package hu.bme.aut.android.puppysitter.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.puppysitter.databinding.FragmentLoadingBinding
import hu.bme.aut.android.puppysitter.model.Dog
import hu.bme.aut.android.puppysitter.model.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.net.URL
import java.util.HashMap

class LoadingMatcherFragment: Fragment() {
    private lateinit var binding: FragmentLoadingBinding
    private lateinit var navController: NavController
    private lateinit var usr: User
    private lateinit var usrType: String
    private lateinit var matchType: String
    private val storage = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoadingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        GlobalScope.launch { setUpMatcher() }
    }

    suspend fun setUpMatcher(){
        usr = activity?.intent?.extras?.get("USER") as User
        usrType = activity?.intent?.extras?.get("USER_TYPE") as String
        matchType = if(usrType == "sitters") "dogs" else "sitters"
        val userData = firestore.collection(usrType).document(usr.uid!!).get().await()
        val matchList = arrayListOf<User>()
        val matchableUIDList = userData["matchables"] as ArrayList<String>
        val pictures = hashMapOf<String, ArrayList<Bitmap>>()
        runBlocking {
            for (uid: String in matchableUIDList) {
                Log.d("SETUPMATCHER", uid)
                val matchData = firestore.collection(matchType).document(uid).get().await()
                val loc = Location("fused")
                loc.latitude = (matchData["location"] as HashMap<String, Any>)["latitude"] as Double
                loc.longitude = (matchData["location"] as HashMap<String, Any>)["longitude"] as Double
                if(usr.location!!.distanceTo(loc) > 1000* usr.range!!){
                    continue
                }
                val match = if(matchType == "dogs"){
                    Dog(matchData["uid"] as String, matchData["email"] as String, matchData["userName"] as String, matchData["realName"] as String, matchData["pictures"] as ArrayList<String>, matchData["bio"] as String, matchData["age"] as Long, matchData["range"] as Long, loc, matchData["breed"] as String?, matchData["weight"] as Long, matchData["activity"] as Long)
                } else {
                    User(matchData["uid"] as String, matchData["email"] as String, matchData["userName"] as String, matchData["realName"] as String, matchData["pictures"] as ArrayList<String>, matchData["bio"] as String, matchData["age"] as Long, matchData["range"] as Long, loc)
                }
                matchList.add(match)
                val picList = match.pictures
                val images = arrayListOf<Bitmap>()
                runBlocking {
                    for (pic: String in picList) {
                        val uri = storage.child(pic).downloadUrl.await()
                        val url = URL(uri.toString())
                        images.add(
                            BitmapFactory.decodeStream(
                                url.openConnection().getInputStream()
                            )
                        )
                    }
                }
                pictures[matchData["uid"] as String] = images
            }
        }
        if(matchList.isEmpty()){
            val action = LoadingMatcherFragmentDirections.actionLoadingFragment2ToEmptyMatchablesFragment()
            activity?.runOnUiThread { navController.navigate(action) }
            return
        }
        if(usrType == "sitters"){
            val action = LoadingMatcherFragmentDirections.actionLoadingFragment2ToMatchSitterFragment((matchList as ArrayList<Dog>).toTypedArray(),usr)
            activity?.runOnUiThread { navController.navigate(action) }
        } else {
            val action = LoadingMatcherFragmentDirections.actionLoadingFragment2ToMatchDogFragment(matchList.toTypedArray(),usr as Dog)
            activity?.runOnUiThread { navController.navigate(action) }
        }
    }
}