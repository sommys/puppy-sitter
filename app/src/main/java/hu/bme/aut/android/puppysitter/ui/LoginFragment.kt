package hu.bme.aut.android.puppysitter.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import hu.bme.aut.android.puppysitter.R
import java.io.InputStream

class LoginFragment(val activityFragmentManager: FragmentManager) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_login, container, false)
        val logoImage: ImageView = root.findViewById(R.id.ivLogo)
        val imageStream: InputStream = resources.openRawResource(R.raw.app_logo)
        val bitmap: Bitmap = BitmapFactory.decodeStream(imageStream)
        logoImage.setImageBitmap(bitmap)
        val registerButton: Button = root.findViewById(R.id.btnSignUp)
        registerButton.setOnClickListener {
            val fragmentTransaction: FragmentTransaction = activityFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frameLayout, RegisterFragment())
            fragmentTransaction.commit()
            Log.d("FOS", "FRAGMENTCSERE")
        }
        return root
    }
}