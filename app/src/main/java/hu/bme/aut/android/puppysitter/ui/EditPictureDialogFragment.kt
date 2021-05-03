package hu.bme.aut.android.puppysitter.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.puppysitter.EditProfileActivity
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.databinding.EditProfilePicturesBinding
import hu.bme.aut.android.puppysitter.databinding.FragmentEditPictureDialogBinding

class EditPictureDialogFragment(val iv: ImageView, val picturesLayout: ConstraintLayout): DialogFragment() {

    private lateinit var binding: FragmentEditPictureDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEditPictureDialogBinding.inflate(layoutInflater)
        if(iv.id == R.id.img1 && picturesLayout.findViewById<ImageView>(R.id.img2).contentDescription == "stock"){
            binding.btnDelete.visibility=View.INVISIBLE
            binding.btnDelete.isClickable=false
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.picture.setImageBitmap(iv.drawable.toBitmap())
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnDelete.setOnClickListener {
            if(iv.id == R.id.img1 && picturesLayout.findViewById<ImageView>(R.id.img2).contentDescription == "stock"){
                Toast.makeText(context, "You can't delete the last photo!", Toast.LENGTH_SHORT).show()
            } else {
                FirebaseStorage.getInstance().reference.child("images/${FirebaseAuth.getInstance().currentUser.uid}/${iv.contentDescription}").delete().addOnSuccessListener {
                    iv.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_plus_circle_black_48dp))
                    iv.contentDescription = "stock"
                    EditProfileActivity.rearrangePhotos(activity as EditProfileActivity)
                    dismiss()
                }
            }


        }
        binding.btnGallery.setOnClickListener {
            UploadPictureFragment(iv).show(requireActivity().supportFragmentManager, "")
            dismiss()
        }
    }


}