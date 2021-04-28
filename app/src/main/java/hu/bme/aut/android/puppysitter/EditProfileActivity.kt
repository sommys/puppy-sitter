package hu.bme.aut.android.puppysitter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.graphics.drawable.toBitmap
import hu.bme.aut.android.puppysitter.databinding.ActivityEditProfileBinding
import hu.bme.aut.android.puppysitter.ui.EditDetailsFragment
import hu.bme.aut.android.puppysitter.ui.EditPictureDialogFragment

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
//        binding.picturesLayout.setOnItemClickListener{ adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
//            Toast.makeText(this, "${view1.id}", Toast.LENGTH_SHORT).show()
//        }
        binding.picturesLayout.img1.setImageBitmap(BitmapFactory.decodeStream(resources.openRawResource(R.raw.penny1)))
        binding.picturesLayout.img1.setOnClickListener {
            EditPictureDialogFragment(binding.picturesLayout.img1.drawable.toBitmap()).show(supportFragmentManager, "")
        }
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().add(R.id.editDetailsFragment, EditDetailsFragment(intent.extras?.get("USER_TYPE") as String)).commit()
        val pictures = mutableListOf<Bitmap>()
        val pennyke: Bitmap = BitmapFactory.decodeStream(resources.openRawResource(R.raw.penny1))
        for(i in 0 .. 8){
            pictures.add(i, pennyke)
        }
    }
}