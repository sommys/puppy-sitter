package hu.bme.aut.android.puppysitter

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import hu.bme.aut.android.puppysitter.databinding.ActivityProfileDogBinding
import hu.bme.aut.android.puppysitter.databinding.ActivityProfileSitterBinding
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {
    private lateinit var bindingDog: ActivityProfileDogBinding
    private lateinit var bindingSitter: ActivityProfileSitterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(intent.extras?.get("USER_TYPE") == "SITTER"){
            bindingSitter = ActivityProfileSitterBinding.inflate(layoutInflater)
            setContentView(bindingSitter.root)
            val imageStream: InputStream = resources.openRawResource(R.raw.szemcso1)
            val bitmap: Bitmap = BitmapFactory.decodeStream(imageStream)
            bindingSitter.ivProfilePicture.setImageBitmap(bitmap)
            bindingSitter.btnEditProfile.setOnClickListener {
                startActivity(Intent(this, EditProfileActivity::class.java).putExtra("USER_TYPE", "SITTER"))
            }
        } else {
            bindingDog = ActivityProfileDogBinding.inflate(layoutInflater)
            setContentView(bindingDog.root)
            val imageStream: InputStream = resources.openRawResource(R.raw.szemcso2)
            val bitmap: Bitmap = BitmapFactory.decodeStream(imageStream)
            bindingDog.ivProfilePicture.setImageBitmap(bitmap)

            bindingDog.btnEditProfile.setOnClickListener {
                startActivity(Intent(this, EditProfileActivity::class.java).putExtra("USER_TYPE", "DOG"))
            }
        }
    }

    override fun onBackPressed() {
        //TODO: logout
        val intent = Intent(this, StartActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}