package hu.bme.aut.android.puppysitter.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import androidx.fragment.app.Fragment
import hu.bme.aut.android.puppysitter.R


class EditDetailsFragment(val userType: String) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if(userType == "DOG"){
            val root = inflater.inflate(R.layout.fragment_edit_details_dog, container, false)
            root
        } else {
            val root = inflater.inflate(R.layout.fragment_edit_details_sitter, container, false)
            root
        }
    }
}