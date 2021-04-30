package hu.bme.aut.android.puppysitter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import hu.bme.aut.android.puppysitter.databinding.ActivityEditProfileBinding
import hu.bme.aut.android.puppysitter.ui.EditDetailsFragment
import hu.bme.aut.android.puppysitter.ui.EditPictureDialogFragment
import hu.bme.aut.android.puppysitter.ui.UploadPictureFragment
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

class EditProfileActivity : AppCompatActivity() {
    companion object{
        fun getPicturePaths(epa: EditProfileActivity): ArrayList<String>{
            val ret: ArrayList<String> = arrayListOf()
            for(i: ImageView in epa.pictureHolders){
                if(i.contentDescription != "stock"){
                    ret.add("${epa.pathPrefix}/${i.id}")
                } else {
                    break
                }
            }
            return ret
        }

        fun getFirstEmptyImageView(epa: EditProfileActivity):ImageView?{
            for(i: ImageView in epa.pictureHolders){
                if(i.contentDescription == "stock")
                    return i
            }
            return null
        }
        //not used
        fun savePictures(epa: EditProfileActivity, store: StorageReference,path: String): ArrayList<String>{
            val ret: ArrayList<String> = arrayListOf()
            for(i: ImageView in epa.pictureHolders){
                if(i.contentDescription != "stock"){
                    val baos = ByteArrayOutputStream()
                    i.drawable.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    var uploadTask = store.child(i.contentDescription.toString()).putBytes(data)
                    uploadTask.addOnFailureListener {
                    }.addOnSuccessListener { taskSnapshot ->
                        ret.add(taskSnapshot.storage.path)
                        i.contentDescription = taskSnapshot.storage.path
                    }
                } else {
                    break
                }
            }
            return ret
        }
    }
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var pictureHolders: ArrayList<ImageView>
    private lateinit var pathPrefix: String
    private lateinit var usrType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        pathPrefix = "images/${FirebaseAuth.getInstance().currentUser?.uid}"
        setOnImageClickListeners()
        binding.btnCancel.setOnClickListener {
            finish()
        }
        usrType = (intent.extras?.get("USER_TYPE") as String).toLowerCase()
        setPictureHolders()
        initalizePictures()
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().add(R.id.editDetailsFragment, EditDetailsFragment(intent.extras?.get("USER_TYPE") as String)).commit()
    }

    override fun onDestroy() {
        val picturePaths: ArrayList<String> = arrayListOf()
        for(iv: ImageView in pictureHolders){
            if(iv.contentDescription != "stock"){
                picturePaths.add("$pathPrefix/${iv.id}")
            } else {
                break
            }
        }
        FirebaseFirestore.getInstance().collection("${usrType}s").document(FirebaseAuth.getInstance().currentUser!!.uid).update(mapOf("pictures" to picturePaths))
        super.onDestroy()
    }

    fun initalizePictures() = GlobalScope.async{
        var i = 0
        val storageRef = FirebaseStorage.getInstance().reference
        FirebaseFirestore.getInstance().collection("${usrType}s")
            .document(FirebaseAuth.getInstance().currentUser!!.uid).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val picturePaths = it.result?.get("pictures") as ArrayList<String>
                    for (path: String in picturePaths) {
                        val picHolder = pictureHolders[i++]
                        storageRef.child(path).downloadUrl.addOnSuccessListener {
                            Glide.with(this@EditProfileActivity).load(it).into(picHolder)
                            picHolder.contentDescription = picHolder.id.toString()
                        }
                    }
                }
            }

    }

    fun setPictureHolders() {
        pictureHolders = arrayListOf(
            binding.picturesLayout.img1,
            binding.picturesLayout.img2,
            binding.picturesLayout.img3,
            binding.picturesLayout.img4,
            binding.picturesLayout.img5,
            binding.picturesLayout.img6,
            binding.picturesLayout.img7,
            binding.picturesLayout.img8,
            binding.picturesLayout.img9
        )
    }

    fun addImageBitmap(iv:ImageView, img: Bitmap){
        if(img.byteCount > 4*1024*1024){
            Toast.makeText(this, "Image size too big! [Maximum Size: 4MB]", Toast.LENGTH_SHORT).show()
            return
        }
        val store = FirebaseStorage.getInstance().reference.child("images/${FirebaseAuth.getInstance().currentUser?.uid}")
        val baos = ByteArrayOutputStream()
        iv.setImageBitmap(img)
        iv.drawable.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        iv.contentDescription = "${iv.id}"
        var uploadTask = store.child(iv.contentDescription.toString()).putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->

        }

    }

    fun addImage(iv: ImageView, imgId: Int) {
        iv.setImageBitmap(BitmapFactory.decodeStream(resources.openRawResource(imgId)))
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