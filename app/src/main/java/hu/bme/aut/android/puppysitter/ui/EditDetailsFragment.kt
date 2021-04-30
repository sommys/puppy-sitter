package hu.bme.aut.android.puppysitter.ui

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.databinding.FragmentEditDetailsDogBinding
import hu.bme.aut.android.puppysitter.databinding.FragmentEditDetailsSitterBinding
import hu.bme.aut.android.puppysitter.model.Sitter
import hu.bme.aut.android.puppysitter.model.User
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
                firebaseDB.collection("sitters").document(currentUser.uid).set(Sitter(currentUser?.email,currentUser?.displayName,"testBio", parseInt(bindingSitter.itAge.text.toString()), Location("fused")))
            }
            bindingSitter.root
        }
    }
}