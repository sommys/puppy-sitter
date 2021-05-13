package hu.bme.aut.android.puppysitter.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
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
import hu.bme.aut.android.puppysitter.databinding.FragmentEditDogBinding
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.initializePictures
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.saveChanges
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.savePictures
import kotlinx.coroutines.*
import java.lang.Long

class EditDogFragment: Fragment() {
    private val args: EditDogFragmentArgs by navArgs()
    private lateinit var pathPrefix: String
    lateinit var pictureHolders: ArrayList<ImageView>
    private lateinit var navController: NavController
    private lateinit var binding: FragmentEditDogBinding
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
        binding = FragmentEditDogBinding.inflate(layoutInflater)
        setPictureHolders()
        pathPrefix = "images/${args.usr.uid}"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnImageClickListeners()
        GlobalScope.launch { initializePictures(requireActivity(),pictureHolders, "dogs", args.usr) }
        navController = Navigation.findNavController(view)
        binding.editDetailsDog.itAge.setText(args.usr.age.toString())
        binding.editDetailsDog.itBio.setText(args.usr.bio)
        binding.editDetailsDog.itBreed.setText(args.usr.breed)
        binding.editDetailsDog.itWeight.setText(args.usr.weight.toString())
        binding.editDetailsDog.itActivity.setText(args.usr.activity.toString())
        binding.editDetailsDog.rangeSlider.value = args.usr.range?.toFloat() ?: 5F
        binding.editDetailsDog.btnSubmit.setOnClickListener {
            modifyUserData()
            GlobalScope.launch {
                savePictures(pictureHolders, "dogs", args.usr)
                args.usr.pictures = getPicturePaths()
                saveChanges("dogs", args.usr)
                withContext(Dispatchers.Main){
                    Toast.makeText(this@EditDogFragment.activity, "Changes saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.btnBack.setOnClickListener {
            modifyUserData()
            GlobalScope.launch {
                savePictures(pictureHolders, "dogs", args.usr)
                args.usr.pictures = getPicturePaths()
                saveChanges("dogs", args.usr)
                withContext(Dispatchers.Main){
                    Toast.makeText(this@EditDogFragment.activity, "Changes saved!", Toast.LENGTH_SHORT).show()
                }
            }
            val action = EditDogFragmentDirections.actionEditDogFragmentToLoadingProfileFragment()
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
            UploadPictureDialogFragment(getFirstEmptyImageView()!!).show(parentFragmentManager, "")
        } else {
            EditPictureDialogFragment(it, this, "dogs").show(parentFragmentManager, "")
        }
    }

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
        GlobalScope.launch{savePictures(pictureHolders,"dogs", args.usr)}
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
        args.usr.age = Long.parseLong(binding.editDetailsDog.itAge.text.toString())
        args.usr.bio = binding.editDetailsDog.itBio.text.toString()
        args.usr.range = binding.editDetailsDog.rangeSlider.value.toLong()
        args.usr.weight = Long.parseLong(binding.editDetailsDog.itWeight.text.toString())
        args.usr.activity = Long.parseLong(binding.editDetailsDog.itActivity.text.toString())
        args.usr.breed = binding.editDetailsDog.itBreed.text.toString()
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