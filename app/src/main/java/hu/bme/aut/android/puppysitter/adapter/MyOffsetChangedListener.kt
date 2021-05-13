package hu.bme.aut.android.puppysitter.adapter

import android.view.View
import android.widget.ImageButton
import com.google.android.material.appbar.AppBarLayout

class MyOffsetChangedListener(private val btnDown: ImageButton): AppBarLayout.BaseOnOffsetChangedListener<AppBarLayout>{
    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if(verticalOffset < 0){
            btnDown.visibility = View.VISIBLE
            btnDown.isClickable = true
        } else {
            btnDown.visibility = View.INVISIBLE
            btnDown.isClickable = false
        }
    }
}