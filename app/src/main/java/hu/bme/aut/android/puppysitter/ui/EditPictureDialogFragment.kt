package hu.bme.aut.android.puppysitter.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import hu.bme.aut.android.puppysitter.R
import hu.bme.aut.android.puppysitter.databinding.FragmentEditPictureDialogBinding

class EditPictureDialogFragment(val iv: ImageView): DialogFragment() {

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
            //TODO delete the pic from the users piclist
            iv.setImageBitmap(BitmapFactory.decodeResource(resources,R.drawable.ic_plus_circle_black_48dp))
            iv.contentDescription="stock"
            dismiss()
        }
        binding.btnGallery.setOnClickListener {
            //TODO picture picker dialog --> amit kivalaszt, azt berakni a mostani helyett
            UploadPictureFragment(iv).show(requireActivity().supportFragmentManager, "")
            dismiss()
        }
    }
}