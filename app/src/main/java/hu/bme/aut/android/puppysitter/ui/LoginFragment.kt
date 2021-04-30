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
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.android.puppysitter.ProfileActivity
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.adapter.FirebaseAuthHelper.Companion.login
import hu.bme.aut.android.puppysitter.databinding.FragmentLoginBinding
import hu.bme.aut.android.puppysitter.extensions.validateNonEmpty
import java.io.InputStream

class LoginFragment(val activityFragmentManager: FragmentManager) : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        binding.ivLogo.setImageBitmap(BitmapFactory.decodeStream(resources.openRawResource(R.raw.app_logo)))
        binding.btnSignUp.setOnClickListener {
            val ft: FragmentTransaction = activityFragmentManager.beginTransaction()
            ft.replace(R.id.frameLayout, RegisterFragment())
            ft.addToBackStack("loginFragment")
            ft.commit()
        }
        binding.btnLogin.setOnClickListener{
            //TODO login --> find user object, load data to make it available everywhere
            if(validateLogin())
                login(activity, binding.itLoginEmail.text.toString(), binding.itPassword.text.toString())
        }
        return binding.root
    }

    private fun validateLogin() = binding.itLoginEmail.validateNonEmpty() && binding.itPassword.validateNonEmpty()
}