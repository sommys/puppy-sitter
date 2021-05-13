package hu.bme.aut.android.puppysitter.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.databinding.FragmentEditPictureDialogBinding
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.deletePicture
import hu.bme.aut.android.puppysitter.model.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditPictureDialogFragment(val iv: ImageView, val fragment: Fragment, val usr: User, val usrType: String, val lastPicture: Boolean): DialogFragment() {
    private lateinit var binding: FragmentEditPictureDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEditPictureDialogBinding.inflate(layoutInflater)
        if(lastPicture){
            binding.btnDelete.visibility=View.INVISIBLE
            binding.btnDelete.isClickable=false
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.picture.setImageBitmap(iv.drawable.toBitmap())
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnDelete.setOnClickListener {
            GlobalScope.launch {
                usr.pictures.remove("images/${usr.uid}/${iv.contentDescription}")
                deletePicture(usr, usrType, iv, resources, fragment)
            }
            dismiss()
        }
        binding.btnEdit.setOnClickListener {
            UploadPictureDialogFragment(fragment, iv, true, usr, usrType).show(fragment.parentFragmentManager, "")
            dismiss()
        }
    }
}