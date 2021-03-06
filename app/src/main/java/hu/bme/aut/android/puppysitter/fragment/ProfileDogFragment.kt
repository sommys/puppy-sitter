package hu.bme.aut.android.puppysitter.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import hu.bme.aut.android.puppysitter.MatcherActivity
import hu.bme.aut.android.puppysitter.databinding.FragmentProfileDogBinding
import hu.bme.aut.android.puppysitter.helper.NotificationHelper
import hu.bme.aut.android.puppysitter.model.User


class ProfileDogFragment() : Fragment() {
    val args: ProfileDogFragmentArgs by navArgs()
    private lateinit var navController: NavController
    private lateinit var binding: FragmentProfileDogBinding
    private lateinit var notiHelper: NotificationHelper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileDogBinding.inflate(layoutInflater)
        notiHelper = NotificationHelper(activity?.applicationContext!!)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        Glide.with(this).load(args.profilePicture).into(binding.ivProfilePicture)
//        binding.ivProfilePicture.setImageBitmap(args.profilePicture)
        binding.profileDetails.tvUsername.text = args.usr.userName
        binding.profileDetails.tvRealName.text = args.usr.name
        if(args.usr.age ?: 0L != 0L){
            binding.profileDetails.tvAge.text = args.usr.age.toString()
        } else {
            binding.profileDetails.tvAge.visibility = View.INVISIBLE
            binding.profileDetails.tvAgeLabel.visibility = View.INVISIBLE
        }
        if(args.usr.bio ?: "" != ""){
            binding.profileDetails.tvBio.text = args.usr.bio.toString()
        } else {
            binding.profileDetails.tvBio.visibility = View.INVISIBLE
            binding.profileDetails.tvBioLabel.visibility = View.INVISIBLE
        }
        if(args.usr.activity ?: 0L != 0L){
            binding.profileDetails.tvActivity.text = args.usr.activity.toString()
        } else {
            binding.profileDetails.tvActivity.visibility = View.INVISIBLE
            binding.profileDetails.tvActivityLabel.visibility = View.INVISIBLE
        }
        if(args.usr.breed ?: "" != ""){
            binding.profileDetails.tvBreed.text =  args.usr.breed
        } else {
            binding.profileDetails.tvBreed.visibility = View.INVISIBLE
            binding.profileDetails.tvBreedLabel.visibility = View.INVISIBLE
        }
        if(args.usr.weight ?: 0L != 0L){
            binding.profileDetails.tvWeight.text = args.usr.weight.toString()
        } else {
            binding.profileDetails.tvWeight.visibility = View.INVISIBLE
            binding.profileDetails.tvWeightLabel.visibility = View.INVISIBLE
        }

        binding.btnMatch.setOnClickListener {
            val intent = Intent(activity, MatcherActivity::class.java).putExtra("USER_TYPE", "dogs").putExtra("USER", args.usr)
            activity?.startActivity(intent)
            activity?.finish()
        }
        binding.btnEditProfile.setOnClickListener {
            val action = ProfileDogFragmentDirections.actionProfileDogFragmentToEditDogFragment2(args.usr)
            navController.navigate(action)
        }
        for(match: User in args.awayMatches){
            notiHelper.showMatchNotification(match, "dogs")
        }
    }
}