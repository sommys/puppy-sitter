package hu.bme.aut.android.puppysitter.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.adapter.MyOffsetChangedListener
import hu.bme.aut.android.puppysitter.adapter.ViewPagerAdapter
import hu.bme.aut.android.puppysitter.databinding.FragmentMatchDogBinding
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper
import hu.bme.aut.android.puppysitter.helper.NotificationHelper
import hu.bme.aut.android.puppysitter.model.User
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.net.URL

class MatchDogFragment(): Fragment() {
    val args: MatchDogFragmentArgs by navArgs()

    private lateinit var binding: FragmentMatchDogBinding
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var notiHelper: NotificationHelper
    private lateinit var navController: NavController

    private val matchList= arrayListOf<User>()
    private val storage = FirebaseStorage.getInstance().reference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchDogBinding.inflate(layoutInflater)
        binding.appbar.addOnOffsetChangedListener(MyOffsetChangedListener(binding.btnDown))
        matchList.addAll(args.match)
        notiHelper = NotificationHelper(activity?.applicationContext!!)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        var currentMatch = matchList.removeAt(0)
        GlobalScope.launch { showNextMatch(currentMatch) }
        binding.btnYes.setOnClickListener{
            GlobalScope.launch{
                val successfulMatch =
                    FirebaseHelper.matchPressed("dogs", args.usr, "sitters", currentMatch)
                if(successfulMatch){
                    notiHelper.showMatchNotification(currentMatch, "dogs")
                }
            }
            if(matchList.isEmpty()){
                navController.navigate(R.id.emptyMatchablesFragment)
            } else {
                currentMatch = matchList.removeAt(0)
                GlobalScope.launch { showNextMatch(currentMatch) }
            }
        }
        binding.btnNo.setOnClickListener {
            FirebaseHelper.noPressed("dogs", args.usr, "sitters", currentMatch)
            if(matchList.isEmpty()){
                navController.navigate(R.id.emptyMatchablesFragment)
            } else {
                currentMatch = matchList.removeAt(0)
                GlobalScope.launch { showNextMatch(currentMatch) }
            }
        }
        binding.btnDown.setOnClickListener {
            binding.appbar.setExpanded(true)
        }
    }

    private suspend fun showNextMatch(currentMatch: User) {
        GlobalScope.launch { downloadPictures(currentMatch) }
        activity?.runOnUiThread {
            binding.profileDetails.tvUsername.text = currentMatch.userName
            binding.profileDetails.tvRealName.text = currentMatch.name

            if(currentMatch.age ?: 0L != 0L){
                binding.profileDetails.tvAge.text = currentMatch.age.toString()
            } else {
                binding.profileDetails.tvAge.visibility = View.INVISIBLE
                binding.profileDetails.tvAgeLabel.visibility = View.INVISIBLE
            }
            if(currentMatch.bio ?: "" != ""){
                binding.profileDetails.tvBio.text = currentMatch.bio.toString()
            } else {
                binding.profileDetails.tvBio.visibility = View.INVISIBLE
                binding.profileDetails.tvBioLabel.visibility = View.INVISIBLE
            }
        }
    }

    private suspend fun downloadPictures(currentMatch: User) {
        withContext(Dispatchers.Main){
            binding.toolbarProgressBar.visibility = View.VISIBLE
            binding.pager.visibility = View.INVISIBLE
        }
        val images: ArrayList<String> = arrayListOf()
        runBlocking {
            for (pic: String in currentMatch.pictures) {
                val uri = storage.child(pic).downloadUrl.await()
                images.add(uri.toString())
//                val url = URL(uri.toString())
//                images.add(BitmapFactory.decodeStream(url.openConnection().getInputStream()))
            }

        }
        withContext(Dispatchers.Main){
            adapter = ViewPagerAdapter(requireContext(), layoutInflater, images, binding.appbar)
            binding.pager.adapter = adapter
            binding.toolbarProgressBar.visibility = View.INVISIBLE
            binding.pager.visibility = View.VISIBLE
        }
    }
}