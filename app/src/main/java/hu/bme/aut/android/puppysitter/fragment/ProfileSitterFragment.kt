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
import hu.bme.aut.android.puppysitter.MatcherActivity
import hu.bme.aut.android.puppysitter.databinding.FragmentProfileSitterBinding
import hu.bme.aut.android.puppysitter.helper.NotificationHelper
import hu.bme.aut.android.puppysitter.model.User

class ProfileSitterFragment: Fragment() {
    val args: ProfileSitterFragmentArgs by navArgs()
    private lateinit var navController: NavController
    private lateinit var binding: FragmentProfileSitterBinding
    private lateinit var notiHelper: NotificationHelper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileSitterBinding.inflate(layoutInflater)
        notiHelper = NotificationHelper(activity?.applicationContext!!)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        binding.ivProfilePicture.setImageBitmap(args.profilePicture)
        binding.profileDetails.tvAge.text = args.usr.age.toString()
        binding.profileDetails.tvUsername.text = args.usr.userName
        binding.profileDetails.tvRealName.text = args.usr.name
        binding.profileDetails.tvBio.text = args.usr.bio.toString()
        binding.btnMatch.setOnClickListener {
            val intent = Intent(activity, MatcherActivity::class.java).putExtra("USER_TYPE", activity?.intent?.extras?.get("USER_TYPE") as String).putExtra("USER", activity?.intent?.extras?.get("USER") as User)
            activity?.startActivity(intent)
            activity?.finish()
        }
        binding.btnEditProfile.setOnClickListener {
            val action = ProfileSitterFragmentDirections.actionProfileSitterFragmentToEditSitterFragment2(args.usr)
            navController.navigate(action)
        }
        for(match: User in args.awayMatches){
            notiHelper.showMatchNotification(match, "sitters")
        }
    }
}