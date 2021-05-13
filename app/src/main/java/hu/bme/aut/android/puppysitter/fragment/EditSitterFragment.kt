package hu.bme.aut.android.puppysitter.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.databinding.FragmentEditSitterBinding
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.saveChanges
//import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.savePictures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Long

class EditSitterFragment: Fragment() {
    private val args: EditSitterFragmentArgs by navArgs()
    private lateinit var pathPrefix: String
    lateinit var pictureHolders: ArrayList<ImageView>
    private lateinit var navController: NavController
    private lateinit var binding: FragmentEditSitterBinding
    private lateinit var callback: OnBackPressedCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callback = requireActivity().onBackPressedDispatcher.addCallback(this){
            navController.navigate(R.id.loadingProfileFragment)
        }
        callback.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditSitterBinding.inflate(layoutInflater)
        setPictureHolders()
        pathPrefix = "images/${args.usr.uid}"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnImageClickListeners()
        GlobalScope.launch {
            FirebaseHelper.initializePictures(requireActivity(), pictureHolders, "sitters", args.usr)
        }
        navController = Navigation.findNavController(view)
        binding.editDetailsSitter.itAge.setText(args.usr.age.toString())
        binding.editDetailsSitter.itBio.setText(args.usr.bio)
        binding.editDetailsSitter.rangeSlider.value = args.usr.range?.toFloat() ?: 5F
        binding.editDetailsSitter.btnSubmit.setOnClickListener {
            modifyUserData()
            GlobalScope.launch {
//                savePictures(pictureHolders, "sitters", args.usr)
//                args.usr.pictures = getPicturePaths()
                saveChanges("sitters", args.usr)
                withContext(Dispatchers.Main){
                    Toast.makeText(this@EditSitterFragment.activity, "Changes saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.btnBack.setOnClickListener {
            modifyUserData()
            GlobalScope.launch {
//                savePictures(pictureHolders, "sitters", args.usr)
//                args.usr.pictures = getPicturePaths()
                saveChanges("sitters", args.usr)
                withContext(Dispatchers.Main){
                    Toast.makeText(this@EditSitterFragment.activity, "Changes saved!", Toast.LENGTH_SHORT).show()
                }
            }
            val action = EditSitterFragmentDirections.actionEditSitterFragmentToLoadingProfileFragment()
            navController.navigate(action)
        }
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
        if(it.contentDescription == "stock"){
            UploadPictureDialogFragment(this, getFirstEmptyImageView()!!, false, args.usr, "sitters").show(parentFragmentManager, "")
        } else {
            EditPictureDialogFragment(it, this,args.usr, "sitters", isLast(it)).show(parentFragmentManager, "")
        }
    }

    private fun isLast(it: ImageView): Boolean = it.id == R.id.img1 && binding.picturesLayout.img2.contentDescription == "stock"

    fun rearrangeHolders() {
        val bitmaps: ArrayList<Bitmap> = arrayListOf()
        pictureHolders.forEach { iv ->
            if(iv.contentDescription != "stock") {
                bitmaps.add(iv.drawable.toBitmap())
                iv.setImageBitmap(BitmapFactory.decodeResource(resources,R.drawable.ic_plus_circle_black_48dp))
                iv.contentDescription = "stock"
            }
        }
        var i = 0
        bitmaps.forEach {
            pictureHolders[i].setImageBitmap(it)
            pictureHolders[i].contentDescription = pictureHolders[i].id.toString()
            i++
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

    private fun modifyUserData() {
//        args.usr.pictures = getPicturePaths()
        args.usr.age = Long.parseLong(binding.editDetailsSitter.itAge.text.toString())
        args.usr.bio = binding.editDetailsSitter.itBio.text.toString()
        args.usr.range = binding.editDetailsSitter.rangeSlider.value.toLong()
    }

    private fun getPicturePaths(): ArrayList<String> {
        val ret: ArrayList<String> = arrayListOf()
        for(i: ImageView in pictureHolders){
            if(i.contentDescription != "stock"){
                ret.add("${pathPrefix}/${i.contentDescription}")
            } else {
                break
            }
        }
        return ret
    }

    fun getFirstEmptyImageView():ImageView?{
        for(i: ImageView in pictureHolders){
            if(i.contentDescription == "stock")
                return i
        }
        return null
    }
}