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
import hu.bme.aut.android.puppysitter.ProfileActivity
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.StartActivity
import java.io.InputStream

class RegisterFragment(): Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_register, container, false)

        val logoImage: ImageView = root.findViewById(R.id.ivLogo)
        val imageStream: InputStream = resources.openRawResource(R.raw.nyonya)
        val bitmap: Bitmap = BitmapFactory.decodeStream(imageStream)
        logoImage.setImageBitmap(bitmap)

        val spinner = root.findViewById<Spinner>(R.id.spinnerType)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.usertype_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        val signUpButton = root.findViewById<Button>(R.id.btnLogin)
        signUpButton.setOnClickListener {
            Log.d("REGISTER_EMAIL", root.findViewById<TextInputEditText>(R.id.itEmail).text.toString())
            Log.d("REGISTER_UNAME", root.findViewById<TextInputEditText>(R.id.itUsername).text.toString())
            Log.d("REGISTER_PW", root.findViewById<TextInputEditText>(R.id.itPassword).text.toString())
            Log.d("REGISTER_TYPE", spinner.selectedItem.toString())
            val intent = Intent(activity, ProfileActivity::class.java)
            val type: String = spinner.selectedItem.toString().toUpperCase()
            intent.putExtra("USER_TYPE", type)
            startActivity(intent)
        }
        return root
    }
}