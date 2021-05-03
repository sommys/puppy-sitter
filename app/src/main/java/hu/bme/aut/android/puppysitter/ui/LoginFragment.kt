package hu.bme.aut.android.puppysitter.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.firebase.FirebaseHelper.Companion.login
import hu.bme.aut.android.puppysitter.databinding.FragmentLoginBinding
import hu.bme.aut.android.puppysitter.extensions.validateNonEmpty

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
            if(validateLogin())
                login(activity, binding.itLoginEmail.text.toString(), binding.itPassword.text.toString())
        }
        return binding.root
    }

    private fun validateLogin() = binding.itLoginEmail.validateNonEmpty() && binding.itPassword.validateNonEmpty()
}