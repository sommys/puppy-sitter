package hu.bme.aut.android.puppysitter

import android.Manifest
import android.content.*
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import hu.bme.aut.android.puppysitter.model.User
import hu.bme.aut.android.puppysitter.service.LocationService
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class MatcherActivity : AppCompatActivity() {
    private lateinit var usr: User
    private lateinit var usrType: String
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
        usrType = intent.extras?.get("USER_TYPE") as String
        usr = intent.extras?.get("USER") as User
        Log.d("USRLOCMATCH", usr.location!!.latitude.toString())
        setContentView(R.layout.activity_matcher)
    }

    override fun onStart() {
        super.onStart()

        startLocationServiceWithPermissionCheck()
        registerReceiverWithPermissionCheck()
    }

    override fun onStop() {
        if (locationServiceBinder != null) {
            unbindService(serviceConnection)
            val intent = Intent(this, LocationService::class.java)
            stopService(intent)
        }
        super.onStop()
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

    override fun onBackPressed() {
        startActivity(Intent(this, ProfileActivity::class.java).putExtra("USER_TYPE", intent.extras?.get("USER_TYPE") as String).putExtra("USER", intent.extras?.get("USER") as User))
        finish()
    }
}