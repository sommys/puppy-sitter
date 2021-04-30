package hu.bme.aut.android.puppysitter.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import hu.bme.aut.android.puppysitter.ProfileActivity
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.StartActivity
import hu.bme.aut.android.puppysitter.adapter.FirebaseAuthHelper
import hu.bme.aut.android.puppysitter.databinding.FragmentRegisterBinding
import hu.bme.aut.android.puppysitter.extensions.validateNonEmpty
import java.io.InputStream

class RegisterFragment(): Fragment() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: FragmentRegisterBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        binding.ivLogo.setImageBitmap(BitmapFactory.decodeStream(resources.openRawResource(R.raw.nyonya)))
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.usertype_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerType.adapter = adapter
        }
        binding.btnSignUpLogin.setOnClickListener {
            //TODO add basic profile picture which should be changed later on
            //TODO new reg --> new user object, store it
            //TODO new reg --> put uid into matchablebe collection
            if(validateRegistration())
                FirebaseAuthHelper.register(activity, binding.itRegisterEmail.text.toString(), binding.itRegisterUsername.text.toString(), binding.itPassword.text.toString())
        }
        return binding.root
    }

    private fun validateRegistration() = binding.itRegisterEmail.validateNonEmpty() && binding.itRegisterUsername.validateNonEmpty() && binding.itPassword.validateNonEmpty()
}