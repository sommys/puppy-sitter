package hu.bme.aut.android.puppysitter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.graphics.drawable.toBitmap
import hu.bme.aut.android.puppysitter.databinding.ActivityEditProfileBinding
import hu.bme.aut.android.puppysitter.ui.EditDetailsFragment
import hu.bme.aut.android.puppysitter.ui.EditPictureDialogFragment
import java.io.InputStream

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        binding.picturesLayout.img1.setImageBitmap(BitmapFactory.decodeStream(resources.openRawResource(R.raw.penny1)))
        binding.picturesLayout.img1.setOnClickListener {
            EditPictureDialogFragment(binding.picturesLayout.img1.drawable.toBitmap()).show(supportFragmentManager, "")
        }
        supportFragmentManager.beginTransaction().add(R.id.editDetailsFragment, EditDetailsFragment(intent.extras?.get("USER_TYPE") as String)).commit()
        setContentView(binding.root)
    }
}