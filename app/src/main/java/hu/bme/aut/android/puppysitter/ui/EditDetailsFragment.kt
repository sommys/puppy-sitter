package hu.bme.aut.android.puppysitter.ui

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.puppysitter.EditProfileActivity
import hu.bme.aut.android.puppysitter.databinding.FragmentEditDetailsDogBinding
import hu.bme.aut.android.puppysitter.databinding.FragmentEditDetailsSitterBinding
import hu.bme.aut.android.puppysitter.model.Sitter
import java.lang.Integer.parseInt


class EditDetailsFragment(val userType: String) : Fragment() {
    private lateinit var bindingDog: FragmentEditDetailsDogBinding
    private lateinit var bindingSitter: FragmentEditDetailsSitterBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if(userType == "DOG"){
            bindingDog = FragmentEditDetailsDogBinding.inflate(layoutInflater)
            bindingDog.root
        } else {
            bindingSitter = FragmentEditDetailsSitterBinding.inflate(layoutInflater)
            bindingSitter.btnSubmit.setOnClickListener {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val firebaseDB = FirebaseFirestore.getInstance()

                val usr = Sitter(currentUser?.email,currentUser?.displayName,EditProfileActivity.getPicturePaths(activity as EditProfileActivity),"testBio", parseInt(bindingSitter.itAge.text.toString()), Location("fused"))
                val userData = hashMapOf(
                    "pictures" to usr.pictures,
                    "bio" to usr.bio,
                    "age" to usr.age,
                    "location" to usr.location
                )
                firebaseDB.collection("sitters").document(currentUser!!.uid).update(userData)
            }
            bindingSitter.root
        }
    }


}