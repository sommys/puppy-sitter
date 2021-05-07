package hu.bme.aut.android.puppysitter

import android.Manifest
import android.app.ProgressDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.okhttp.Dispatcher
import hu.bme.aut.android.puppysitter.databinding.ActivityMatcherBinding
import hu.bme.aut.android.puppysitter.databinding.ProfileDetailsDogBinding
import hu.bme.aut.android.puppysitter.databinding.ProfileDetailsSitterBinding
import hu.bme.aut.android.puppysitter.model.Dog
import hu.bme.aut.android.puppysitter.model.User
import hu.bme.aut.android.puppysitter.service.LocationService
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

@RuntimePermissions
class MatcherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMatcherBinding
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var usrType: String
    private lateinit var matchType: String
    private lateinit var usr: User
    private lateinit var currentUserFragment: View
    private var matchables = arrayListOf<User>()
    private val currentUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var locationServiceBinder: LocationService.ServiceLocationBinder? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            locationServiceBinder = binder as LocationService.ServiceLocationBinder
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            locationServiceBinder = null
        }
    }
    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val currentLocation =
                intent.getParcelableExtra<Location>(LocationService.KEY_LOCATION)!!
            usr.location?.latitude  = currentLocation.latitude
            usr.location?.longitude = currentLocation.longitude
            Log.d("LOC_CHANGED_MATCHER", "OK")
            Log.d("LOC_LAT", usr.location?.latitude.toString())
            Log.d("LOC_LON", usr.location?.longitude.toString())
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMatcherBinding.inflate(layoutInflater)
        usrType = intent.extras?.get("USER_TYPE") as String
        matchType = if(usrType == "SITTER") "dogs" else "sitters"
        usr = intent.extras?.getParcelable<User>("USER")!!
        binding.progressBar.visibility = View.VISIBLE
        binding.appbar.visibility = View.INVISIBLE
        GlobalScope.launch{ getMatchables() }
        binding.appbar.addOnOffsetChangedListener(MyOffsetChangedListener())
        binding.btnDown.setOnClickListener {
            binding.appbar.setExpanded(true)
        }
        binding.btnNo.setOnClickListener {
            //TODO remove matchables from the users list
            matchables.removeAt(0)
            GlobalScope.launch { setUserData() }
        }
        binding.btnYes.setOnClickListener {
            //TODO add possible match or made match and notification!

        }
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        startLocationServiceWithPermissionCheck()
        registerReceiverWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun registerReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(locationReceiver, IntentFilter(LocationService.BR_NEW_LOCATION))
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        intent.putExtra("USER_TYPE", usrType)
        intent.putExtra("USER", usr)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        startService(intent)
    }

    suspend fun getMatchables(){
        var matchableList: ArrayList<String>
        val list = firestore.collection("${usrType.toLowerCase()}s").document(currentUser.uid).get().await()
        matchableList = list["matchables"] as ArrayList<String>
        for (uid: String in matchableList) {
            addProfile(uid)
        }
        runOnUiThread{
            binding.progressBar.visibility = View.INVISIBLE
            binding.appbar.visibility = View.VISIBLE
            GlobalScope.launch{setUserData()}
        }
    }

    private suspend fun setUserData() = withContext(Dispatchers.IO){
        runOnUiThread {
            binding.frameLayout.removeAllViews()
        }
        if(!matchables.isEmpty()) {
            runOnUiThread {
                binding.progressBar.visibility = View.VISIBLE
                binding.appbar.visibility = View.INVISIBLE
            }
            var match = matchables[0]
            runBlocking{ setUserPhotos(matchType, match) }
            if (matchType == "dogs") {
                val fragment = ProfileDetailsDogBinding.inflate(layoutInflater)
                fragment.tvUsername.text = match.userName
                fragment.tvRealName.text = match.name

                if(match.age == 0L){
                    fragment.tvAgeLabel.visibility = View.INVISIBLE
                    fragment.tvAge.visibility = View.INVISIBLE
                } else {
                    fragment.tvAge.text = match.age.toString()
                }

                if((match as Dog).breed.isNullOrEmpty()){
                    fragment.tvBreedLabel.visibility = View.INVISIBLE
                    fragment.tvBreed.visibility = View.INVISIBLE
                } else {
                    fragment.tvBreed.text = match.breed
                }

                if(match.weight == 0L){
                    fragment.tvWeightLabel.visibility = View.INVISIBLE
                    fragment.tvWeight.visibility = View.INVISIBLE
                } else {
                    fragment.tvWeight.text = match.weight.toString()
                }

                if(match.activity == 0L){
                    fragment.tvActivityLabel.visibility = View.INVISIBLE
                    fragment.tvActivity.visibility = View.INVISIBLE
                } else {
                    fragment.tvActivity.text = match.activity.toString()
                }

                if(match.bio.isNullOrEmpty()){
                    fragment.tvBioLabel.visibility = View.INVISIBLE
                    fragment.tvBio.visibility = View.INVISIBLE
                } else {
                    fragment.tvBio.text = match.bio
                }

                currentUserFragment = fragment.root
                runOnUiThread{ binding.frameLayout.addView(fragment.root) }
            } else {
                val fragment = ProfileDetailsSitterBinding.inflate(layoutInflater)
                fragment.tvUsername.text = match.userName
                fragment.tvRealName.text = match.name

                if(match.age == 0L){
                    fragment.tvAgeLabel.visibility = View.INVISIBLE
                    fragment.tvAge.visibility = View.INVISIBLE
                } else {
                    fragment.tvAge.text = match.age.toString()
                }

                if(match.bio.isNullOrEmpty()){
                    fragment.tvBioLabel.visibility = View.INVISIBLE
                    fragment.tvBio.visibility = View.INVISIBLE
                } else {
                    fragment.tvBio.text = match.bio
                }

                currentUserFragment = fragment.root
                runOnUiThread{ binding.frameLayout.addView(fragment.root) }
            }
            runOnUiThread{
                binding.progressBar.visibility = View.INVISIBLE
                binding.appbar.visibility = View.VISIBLE
            }
        } else {
            runOnUiThread {
                binding.root.removeView(binding.appbar)
                val noMoreMatches = TextView(this@MatcherActivity)
                noMoreMatches.text = "No more matches available!\nWait for more users to register in your area or update your range!"
                noMoreMatches.textSize = 24F
                var params = CoordinatorLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                params.setMargins(16, 16, 16, 16)
                noMoreMatches.layoutParams = params
                noMoreMatches.gravity = Gravity.CENTER
                binding.frameLayout.addView(noMoreMatches)
            }
        }
    }

    suspend fun setUserPhotos(matchType: String, match: User) {
        val images: ArrayList<Bitmap> = arrayListOf()
        runBlocking {
            for (pic: String in match.pictures) {
                val uri = storage.reference.child(pic).downloadUrl.await()
                val url = URL(uri.toString())
                images.add(BitmapFactory.decodeStream(url.openConnection().getInputStream()))
            }
        }
        withContext(Dispatchers.Main){
            adapter = ViewPagerAdapter(layoutInflater, images)
            binding.pager.adapter = adapter
        }
    }

    suspend fun addProfile(uid: String){
        val match = firestore.collection(matchType).document(uid).get().await()
        var loc = Location("fused")
        loc.latitude = (match["location"] as HashMap<String, Any>)["latitude"] as Double
        loc.longitude = (match["location"] as HashMap<String, Any>)["longitude"] as Double
        if(loc.distanceTo(usr.location!!) <= 1000*usr.range!!) {
            Log.d("ADDPROFILE", match["email"] as String)
            if (matchType == "dogs") {
                matchables.add(Dog(match["uid"] as String, match["email"] as String, match["userName"] as String, match["realName"] as String, match["pictures"] as ArrayList<String>, match["bio"] as String, match["age"] as Long, match["range"] as Long, loc, match["breed"] as String?, match["weight"] as Long, match["activity"] as Long))
            } else {
                matchables.add(User(match["uid"] as String, match["email"] as String, match["userName"] as String, match["realName"] as String, match["pictures"] as ArrayList<String>, match["bio"] as String, match["age"] as Long, match["range"] as Long, loc))
            }
        }
    }

    override fun onStop() {
        if (locationServiceBinder != null) {
            unbindService(serviceConnection)
            val intent = Intent(this, LocationService::class.java)
            stopService(intent)
        }
        super.onStop()
    }

    inner class MyOffsetChangedListener: AppBarLayout.BaseOnOffsetChangedListener<AppBarLayout>{
        override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
            if(verticalOffset < 0){
                binding.btnDown.visibility = View.VISIBLE
                binding.btnDown.isClickable = true
            } else {
                binding.btnDown.visibility = View.INVISIBLE
                binding.btnDown.isClickable = false
            }
        }

    }

    override fun onBackPressed() {
        if(binding.appbar.isLifted){
            binding.appbar.setExpanded(true)
        } else {
            finish()
        }
    }

    inner class ViewPagerAdapter(private val mLayoutInflater: LayoutInflater, private val images: MutableList<Bitmap>): PagerAdapter(){
        override fun getCount(): Int = images.size

        override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj as LinearLayout

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            // inflating the item.xml
            val itemView: View = mLayoutInflater.inflate(R.layout.viewpager_item, container, false)

            // referencing the image view from the item.xml file
            val imageView: ImageView = itemView.findViewById<View>(R.id.imageViewMain) as ImageView

            imageView.setOnClickListener {
                binding.appbar.setExpanded(false)
            }

            // setting the image in the imageView
            imageView.setImageBitmap(images[position])

            // Adding the View
            Objects.requireNonNull(container).addView(itemView)
            return itemView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as LinearLayout)
        }

    }
}