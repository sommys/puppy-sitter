package hu.bme.aut.android.puppysitter

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.android.puppysitter.ui.LoginFragment


class StartActivity : AppCompatActivity() {
    //TODO navigation component....
    //TODO notification
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.frameLayout, LoginFragment(supportFragmentManager))
        ft.commit()
    }

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount > 0){
            supportFragmentManager.popBackStack()
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            ft.replace(R.id.frameLayout, LoginFragment(supportFragmentManager))
            ft.commit()
        } else {
            finishAffinity()
        }
    }
}