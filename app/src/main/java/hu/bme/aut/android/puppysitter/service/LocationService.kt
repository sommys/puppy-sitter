package hu.bme.aut.android.puppysitter.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper
import hu.bme.aut.android.puppysitter.helper.LocationHelper
import hu.bme.aut.android.puppysitter.model.User

class LocationService : Service() {
    private val locationServiceBinder: IBinder = ServiceLocationBinder()
    private var locationHelper: LocationHelper? = null
    private lateinit var usrType: String
    private var location: Location? = null
    private lateinit var usr: User
    var lat: Double = 0.0
    var lon: Double = 0.0
    companion object{
        const val BR_NEW_LOCATION = "BR_NEW_LOCATION"
        const val KEY_LOCATION = "KEY_LOCATION"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LOCATION_CHANGED", "OK")
//        startForeground(LOCATION_NOTIFICATION_ID, createNotification("PuppySitter is using your location"))
        if (locationHelper == null) {
            val helper = LocationHelper(applicationContext, LocationServiceCallback())
            helper.startLocationMonitoring()
            locationHelper = helper
        }
        usrType = intent?.extras?.get("USER_TYPE") as String
        usr = intent.extras?.getParcelable<User>("USER")!!
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        FirebaseHelper.updateLocation(location!!, usrType)
        locationHelper?.stopLocationMonitoring()

        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder = locationServiceBinder

    inner class LocationServiceCallback : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if(location == null){
                Log.d("LOCATION_CHANGED_NULL", "OK")
                lat = result.lastLocation.latitude
                lon = result.lastLocation.longitude
                location = result.lastLocation
                FirebaseHelper.updateLocation(location!!, usrType)
                val intent = Intent()
                intent.action = BR_NEW_LOCATION
                intent.putExtra(KEY_LOCATION, location)
                LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(intent)
            } else if (location!!.distanceTo(result.lastLocation) > 5000){
                Log.d("LOCATION_CHANGED", "OK")
                lat = result.lastLocation.latitude
                lon = result.lastLocation.longitude
                location = result.lastLocation
                FirebaseHelper.updateLocation(location!!, usrType)
                val intent = Intent()
                intent.action = BR_NEW_LOCATION
                intent.putExtra(KEY_LOCATION, location)
                LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(intent)
            }
        }
    }

    inner class ServiceLocationBinder : Binder() {
        val service: LocationService
            get() = this@LocationService
    }
}