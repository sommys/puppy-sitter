package hu.bme.aut.android.puppysitter.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import hu.bme.aut.android.puppysitter.databinding.FragmentUploadPictureDialogBinding

class UploadPictureDialogFragment(val iv: ImageView): DialogFragment() {
    companion object{
        private const val REQUEST_CAMERA_IMAGE = 101
        private const val REQUEST_PICK_IMAGE = 102
    }
    private lateinit var binding: FragmentUploadPictureDialogBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_CAMERA_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val img = data!!.extras!!["data"] as Bitmap
                    if(img.byteCount > 4*1024*1024){
                        Toast.makeText(requireContext(), "Image size too big! [Maximum Size: 4MB]", Toast.LENGTH_SHORT).show()
                        return
                    }
                    iv.setImageBitmap(img)
                    iv.contentDescription = iv.id.toString()
                    dismiss()
                }
            }
            REQUEST_PICK_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val img = MediaStore.Images.Media.getBitmap(context?.contentResolver, data!!.data)
                    if(img.byteCount > 4*1024*1024){
                        Toast.makeText(requireContext(), "Image size too big! [Maximum Size: 4MB]", Toast.LENGTH_SHORT).show()
                        return
                    }
                    iv.setImageBitmap(img)
                    iv.contentDescription = iv.id.toString()
                    dismiss()
                }
            }
        }
    }
}