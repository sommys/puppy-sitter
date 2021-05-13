package hu.bme.aut.android.puppysitter.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import hu.bme.aut.android.puppysitter.databinding.FragmentEditPictureDialogBinding
import hu.bme.aut.android.puppysitter.helper.FirebaseHelper.Companion.deletePicture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditPictureDialogFragment(val iv: ImageView, val fragment: Fragment, val usrType: String): DialogFragment() {
    private lateinit var binding: FragmentEditPictureDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEditPictureDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.picture.setImageBitmap(iv.drawable.toBitmap())
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnDelete.setOnClickListener {
            GlobalScope.launch { deletePicture(usrType, iv, resources, fragment) }
            dismiss()
        }
        binding.btnEdit.setOnClickListener {
            UploadPictureDialogFragment(iv).show(fragment.parentFragmentManager, "")
            dismiss()
        }
    }
}