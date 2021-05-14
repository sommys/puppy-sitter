package hu.bme.aut.android.puppysitter.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.databinding.FragmentRegisterBinding
import hu.bme.aut.android.puppysitter.extensions.validateNonEmpty
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper

class RegisterFragment(): Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        binding.ivLogo.setImageBitmap(BitmapFactory.decodeStream(resources.openRawResource(R.raw.app_logo)))
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.usertype_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerType.adapter = adapter
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignUpLogin.setOnClickListener {
            if(validateRegistration())
                FirebaseHelper.register(activity, binding.itRegisterEmail.text.toString(), binding.itRegisterUsername.text.toString(), binding.itRegisterRealName.text.toString(), binding.itPassword.text.toString(), "${binding.spinnerType.selectedItem.toString().toLowerCase()}s")
        }
    }

    private fun validateRegistration() = binding.itRegisterEmail.validateNonEmpty() && binding.itRegisterUsername.validateNonEmpty() && binding.itRegisterRealName.validateNonEmpty() && binding.itPassword.validateNonEmpty()
}