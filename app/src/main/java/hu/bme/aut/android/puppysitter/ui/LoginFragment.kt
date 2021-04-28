package hu.bme.aut.android.puppysitter.ui

import android.content.Context
import android.content.Intent
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
import com.google.android.material.textfield.TextInputEditText
import hu.bme.aut.android.puppysitter.ProfileActivity
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
        val loginButton: Button = root.findViewById(R.id.btnLogin)
        registerButton.setOnClickListener {
            val ft: FragmentTransaction = activityFragmentManager.beginTransaction()
            ft.replace(R.id.frameLayout, RegisterFragment())
            ft.addToBackStack("loginFragment")
            ft.commit()
        }
        loginButton.setOnClickListener{
            val intent = Intent(activity, ProfileActivity::class.java)
            val type: String = if (root.findViewById<TextInputEditText>(R.id.itUsername).text.isNullOrEmpty()) "SITTER" else "DOG"
            intent.putExtra("USER_TYPE", type)
            startActivity(intent)
        }
        return root
    }
}