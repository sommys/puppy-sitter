package hu.bme.aut.android.puppysitter.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.puppysitter.databinding.FragmentUploadPictureDialogBinding
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.editPicture
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.editPictureCamera
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.uploadPicture
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.uploadPictureCamera
import hu.bme.aut.android.puppysitter.model.User
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.InputStream


class UploadPictureDialogFragment(val callerFragment: Fragment, val iv: ImageView, val edit: Boolean, val usr: User, val usrType: String): DialogFragment() {
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
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                REQUEST_PICK_IMAGE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_CAMERA_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val img = data!!.extras!!["data"] as Bitmap
                    Log.d("IMG_BYTECOUNT", img.allocationByteCount.toString())
                    if (img.allocationByteCount > 4 * 1024 * 1024) {
                        Toast.makeText(requireContext(), "Image size too big! [Maximum Size: 4MB]", Toast.LENGTH_SHORT).show()
                        return
                    }
                    if(edit){
                        binding.root.removeAllViews()
                        binding.root.addView(ProgressBar(requireContext()))
                        GlobalScope.launch {
                            runBlocking { editPictureCamera(usr, usrType, iv, img) }
                            dismiss()
                        }
                    } else {
                        binding.root.removeAllViews()
                        binding.root.addView(ProgressBar(requireContext()))
                        GlobalScope.launch {
                            runBlocking { uploadPictureCamera(usr, usrType, iv, img) }
                            dismiss()
                        }
                    }
                }
            }
            REQUEST_PICK_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri = data!!.data!!
                    val imageName = uri.toString().substring(uri.toString().lastIndexOf("/") + 1)
                    GlobalScope.launch {
                        Log.d(
                            "IMAGEPATH",
                            data!!.data.toString()
                                .substring(data.data.toString().lastIndexOf("/") + 1)
                        )
                        binding.root.removeAllViews()
                        binding.root.addView(ProgressBar(requireContext()))
                        runBlocking {
                            FirebaseStorage.getInstance().reference.child("example/${imageName}")
                                .putFile(uri).await()
                        }
                        dismiss()
                    }
                    val fileInputStream: InputStream =
                        context?.contentResolver?.openInputStream(uri)!!
                    val dataSize = fileInputStream.available()
                    if (dataSize > 4 * 1024 * 1024) {
                        Toast.makeText(
                            requireContext(),
                            "Image size too big! [Maximum Size: 4MB]",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    if (edit) {
                        binding.root.removeAllViews()
                        binding.root.addView(ProgressBar(requireContext()))
                        GlobalScope.launch {
                            runBlocking { editPicture(usr, usrType, iv, uri) }
                            withContext(Dispatchers.Main) {
                                iv.contentDescription = imageName
                                Glide.with(callerFragment).load(uri).into(iv)
                            }
                            dismiss()
                        }
                    } else {
                        binding.root.removeAllViews()
                        binding.root.addView(ProgressBar(requireContext()))
                        GlobalScope.launch {
                            runBlocking { uploadPicture(usr, usrType, iv, uri) }
                            withContext(Dispatchers.Main) {
                                iv.contentDescription = imageName
                                Glide.with(callerFragment).load(uri).into(iv)
                            }
                            dismiss()
                        }
                    }
                }
            }
        }
    }
}