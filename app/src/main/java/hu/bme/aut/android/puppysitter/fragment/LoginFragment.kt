package hu.bme.aut.android.puppysitter.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.databinding.FragmentLoginBinding
import hu.bme.aut.android.puppysitter.extensions.validateNonEmpty
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.login

class LoginFragment() : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        binding.ivLogo.setImageBitmap(BitmapFactory.decodeStream(resources.openRawResource(R.raw.app_logo)))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignUp.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.registerFragment)
        }
        binding.btnLogin.setOnClickListener{
            if(validateLogin())
                login(activity, binding.itLoginEmail.text.toString(), binding.itPassword.text.toString())
        }
    }

    private fun validateLogin() = binding.itLoginEmail.validateNonEmpty() && binding.itPassword.validateNonEmpty()
}