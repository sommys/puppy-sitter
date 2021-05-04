package hu.bme.aut.android.puppysitter

import android.Manifest
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.appbar.AppBarLayout
import hu.bme.aut.android.puppysitter.databinding.ActivityMatcherBinding
import hu.bme.aut.android.puppysitter.model.User
import hu.bme.aut.android.puppysitter.service.LocationService
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.util.*

@RuntimePermissions
class MatcherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMatcherBinding
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var usrType: String
    private lateinit var usr: User
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
        //TODO get matchable profiles [and filter by distance later on]
        //TODO set first matchable profile pictures and data to the layout
        binding = ActivityMatcherBinding.inflate(layoutInflater)
        usrType = intent.extras?.get("USER_TYPE") as String
        usr = intent.extras?.getParcelable<User>("USER")!!
        val images: MutableList<Bitmap> = mutableListOf<Bitmap>()
        images.add(BitmapFactory.decodeStream(resources.openRawResource(R.raw.penny1)))
        images.add(BitmapFactory.decodeStream(resources.openRawResource(R.raw.nyonya)))
        images.add(BitmapFactory.decodeStream(resources.openRawResource(R.raw.szemcso2)))
        adapter = ViewPagerAdapter(layoutInflater, images)
        binding.pager.adapter = adapter
        binding.appbar.addOnOffsetChangedListener(MyOffsetChangedListener())
        binding.btnDown.setOnClickListener {
            binding.appbar.setExpanded(true)
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
            super.onBackPressed()
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