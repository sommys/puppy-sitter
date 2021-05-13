package hu.bme.aut.android.puppysitter.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.adapter.MyOffsetChangedListener
import hu.bme.aut.android.puppysitter.adapter.ViewPagerAdapter
import hu.bme.aut.android.puppysitter.databinding.FragmentMatchSitterBinding
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.matchPressed
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.noPressed
import hu.bme.aut.android.puppysitter.helper.NotificationHelper
import hu.bme.aut.android.puppysitter.model.Dog
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.net.URL

class MatchSitterFragment(): Fragment() {
    val args: MatchSitterFragmentArgs by navArgs()

    private lateinit var binding: FragmentMatchSitterBinding
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var notiHelper: NotificationHelper
    private lateinit var navController: NavController

    private val matchList= arrayListOf<Dog>()
    private val storage = FirebaseStorage.getInstance().reference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchSitterBinding.inflate(layoutInflater)
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
                val successfulMatch = matchPressed("sitters", args.usr, "dogs", currentMatch)
                if(successfulMatch){
                    notiHelper.showMatchNotification(currentMatch, "sitters")
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
            noPressed("sitters", args.usr, "dogs", currentMatch)
            if(matchList.isEmpty()){
                navController.navigate(R.id.emptyMatchablesFragment)
            } else {
                currentMatch = matchList.removeAt(0)
                GlobalScope.launch { showNextMatch(currentMatch) }
            }
        }
    }

    private suspend fun showNextMatch(currentMatch: Dog) {
        GlobalScope.launch { downloadPictures(currentMatch) }
        activity?.runOnUiThread {
            binding.profileDetails.tvAge.text = currentMatch.age.toString()
            binding.profileDetails.tvUsername.text = currentMatch.userName
            binding.profileDetails.tvRealName.text = currentMatch.name
            binding.profileDetails.tvBio.text = currentMatch.bio.toString()
            binding.profileDetails.tvActivity.text = currentMatch.activity.toString()
            binding.profileDetails.tvBreed.text = currentMatch.breed.toString()
            binding.profileDetails.tvWeight.text = currentMatch.weight.toString()
        }
    }

    private suspend fun downloadPictures(currentMatch: Dog) {
        withContext(Dispatchers.Main){
            binding.toolbarProgressBar.visibility = View.VISIBLE
            binding.pager.visibility = View.INVISIBLE
        }
        val images: ArrayList<Bitmap> = arrayListOf()
        runBlocking {
            for (pic: String in currentMatch.pictures) {
                val uri = storage.child(pic).downloadUrl.await()
                val url = URL(uri.toString())
                images.add(BitmapFactory.decodeStream(url.openConnection().getInputStream()))
            }
        }
        withContext(Dispatchers.Main){
            adapter = ViewPagerAdapter(layoutInflater, images, binding.appbar)
            binding.pager.adapter = adapter
            binding.toolbarProgressBar.visibility = View.INVISIBLE
            binding.pager.visibility = View.VISIBLE
        }
    }
}