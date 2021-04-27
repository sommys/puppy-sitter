package hu.bme.aut.android.puppysitter.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import hu.bme.aut.android.puppysitter.R
import java.io.InputStream

class RegisterFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_register, container, false)
        val logoImage: ImageView = root.findViewById(R.id.ivLogo)
        val imageStream: InputStream = resources.openRawResource(R.raw.nyonya)
        val bitmap: Bitmap = BitmapFactory.decodeStream(imageStream)
        logoImage.setImageBitmap(bitmap)
        return root
    }
}