package hu.bme.aut.android.puppysitter.ui

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import hu.bme.aut.android.puppysitter.EditProfileActivity
import hu.bme.aut.android.puppysitter.databinding.FragmentUploadPictureDialogBinding


class UploadPictureFragment(val iv: ImageView): DialogFragment() {
    companion object {
        private const val TMP_IMAGE_JPG = "tmp_image.jpg"
        private const val REQUEST_CAMERA_IMAGE = 101
        private const val REQUEST_PICK_IMAGE = 102
    }
    private lateinit var binding: FragmentUploadPictureDialogBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUploadPictureDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnCamera.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, REQUEST_CAMERA_IMAGE)
        }
        binding.btnGallery.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_CAMERA_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val img = data!!.extras!!["data"] as Bitmap
                    if(iv.contentDescription == "stock")
                        (activity as EditProfileActivity).addImageBitmap(EditProfileActivity.getFirstEmptyImageView(activity as EditProfileActivity)!!, img)
                    else
                        (activity as EditProfileActivity).addImageBitmap(iv, img)
                    dismiss()
                }
            }
            REQUEST_PICK_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val img = MediaStore.Images.Media.getBitmap((activity as EditProfileActivity).contentResolver, data!!.data)
                    if(iv.contentDescription == "stock")
                        (activity as EditProfileActivity).addImageBitmap(EditProfileActivity.getFirstEmptyImageView(activity as EditProfileActivity)!!, img)
                    else
                        (activity as EditProfileActivity).addImageBitmap(iv, img)
                    dismiss()
                }
            }
        }
    }
}