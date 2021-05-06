package hu.bme.aut.android.puppysitter.ui

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.android.puppysitter.EditProfileActivity
import hu.bme.aut.android.puppysitter.EditProfileActivity.Companion.getPicturePaths
import hu.bme.aut.android.puppysitter.ProfileActivity
import hu.bme.aut.android.puppysitter.databinding.FragmentEditDetailsDogBinding
import hu.bme.aut.android.puppysitter.databinding.FragmentEditDetailsSitterBinding
import hu.bme.aut.android.puppysitter.model.Dog
import hu.bme.aut.android.puppysitter.model.User
import java.lang.Integer.parseInt
import java.lang.Long.parseLong


class EditDetailsFragment(val userType: String, var usr: User) : Fragment() {
    private lateinit var bindingDog: FragmentEditDetailsDogBinding
    private lateinit var bindingSitter: FragmentEditDetailsSitterBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if(userType == "DOG"){
            bindingDog = FragmentEditDetailsDogBinding.inflate(layoutInflater)
            bindingDog.itAge.setText(usr.age.toString())
            bindingDog.itBio.setText(usr.bio)
            bindingDog.itWeight.setText((usr as Dog).weight.toString())
            bindingDog.itActivity.setText((usr as Dog).activity.toString())
            bindingDog.itBreed.setText((usr as Dog).breed)

            bindingDog.btnSubmit.setOnClickListener {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val firebaseDB = FirebaseFirestore.getInstance()
                usr.pictures = getPicturePaths(activity as EditProfileActivity)
                usr.age = parseLong(bindingDog.itAge.text.toString())
                usr.bio = bindingDog.itBio.text.toString()
                (usr as Dog).weight = parseLong(bindingDog.itWeight.text.toString())
                (usr as Dog).activity = parseLong(bindingDog.itActivity.text.toString())
                (usr as Dog).breed = bindingDog.itBreed.text.toString()
                val userData = hashMapOf(
                        "pictures" to usr.pictures,
                        "bio" to usr.bio,
                        "age" to usr.age,
                        "weight" to (usr as Dog).weight,
                        "activity" to (usr as Dog).activity,
                        "breed" to (usr as Dog).breed
                )
                firebaseDB.collection("dogs").document(currentUser!!.uid).update(userData as Map<String, Any>).addOnSuccessListener {
                    Toast.makeText(activity, "Changes saved!", Toast.LENGTH_SHORT).show()
                }
            }
            bindingDog.root
        } else {
            bindingSitter = FragmentEditDetailsSitterBinding.inflate(layoutInflater)
            bindingSitter.itAge.setText(usr.age.toString())
            bindingSitter.itBio.setText(usr.bio)

            bindingSitter.btnSubmit.setOnClickListener {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val firebaseDB = FirebaseFirestore.getInstance()
                usr.pictures = getPicturePaths(activity as EditProfileActivity)
                usr.age = parseLong(bindingSitter.itAge.text.toString())
                usr.bio = bindingSitter.itBio.text.toString()
                val userData = hashMapOf(
                        "pictures" to usr.pictures,
                        "bio" to usr.bio,
                        "age" to usr.age
                )
                firebaseDB.collection("sitters").document(currentUser!!.uid).update(userData as Map<String, Any>).addOnSuccessListener {
                    Toast.makeText(activity, "Changes saved!", Toast.LENGTH_SHORT).show()
                }
            }
            bindingSitter.root
        }
    }


}