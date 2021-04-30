package hu.bme.aut.android.puppysitter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import hu.bme.aut.android.puppysitter.databinding.ActivityEditProfileBinding
import hu.bme.aut.android.puppysitter.ui.EditDetailsFragment
import hu.bme.aut.android.puppysitter.ui.EditPictureDialogFragment
import hu.bme.aut.android.puppysitter.ui.UploadPictureFragment

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        //TODO lekerni a kepeket es amennyi van, beallitani
        // ...
        setOnImageClickListeners()
        binding.btnCancel.setOnClickListener {
            finish()
        }
        addImage(binding.picturesLayout.img1, R.raw.penny1)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().add(R.id.editDetailsFragment, EditDetailsFragment(intent.extras?.get("USER_TYPE") as String)).commit()
    }

    fun addImageBitmap(iv:ImageView, img: Bitmap){
        iv.setImageBitmap(img)
        //TODO tarolni a profilban
        iv.contentDescription = "${img.generationId}"
    }

    fun addImage(iv: ImageView, imgId: Int) {
        iv.setImageBitmap(BitmapFactory.decodeStream(resources.openRawResource(imgId)))
        //TODO tarolni a profilban
        iv.contentDescription = "${imgId}"
    }

    private fun setOnImageClickListeners() {
        binding.picturesLayout.img1.setOnClickListener { onImageClick(it as ImageView) }
        binding.picturesLayout.img2.setOnClickListener { onImageClick(it as ImageView) }
        binding.picturesLayout.img3.setOnClickListener { onImageClick(it as ImageView) }
        binding.picturesLayout.img4.setOnClickListener { onImageClick(it as ImageView) }
        binding.picturesLayout.img5.setOnClickListener { onImageClick(it as ImageView) }
        binding.picturesLayout.img6.setOnClickListener { onImageClick(it as ImageView) }
        binding.picturesLayout.img7.setOnClickListener { onImageClick(it as ImageView) }
        binding.picturesLayout.img8.setOnClickListener { onImageClick(it as ImageView) }
        binding.picturesLayout.img9.setOnClickListener { onImageClick(it as ImageView) }
    }

    private fun onImageClick(it: ImageView) {
        if(it.contentDescription != "stock")
            EditPictureDialogFragment(it).show(supportFragmentManager, "")
        else
            UploadPictureFragment(it).show(supportFragmentManager, "")
    }
}