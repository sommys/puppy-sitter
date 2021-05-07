package hu.bme.aut.android.puppysitter.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

class LocationHelper(private val context: Context, private val callback: LocationCallback) {

    fun startLocationMonitoring() {
        val request = LocationRequest().apply {
            interval = 5000L
            fastestInterval = 5000L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.getFusedLocationProviderClient(context)
                .requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    fun stopLocationMonitoring() {
        LocationServices.getFusedLocationProviderClient(context).removeLocationUpdates(callback)
    }
}