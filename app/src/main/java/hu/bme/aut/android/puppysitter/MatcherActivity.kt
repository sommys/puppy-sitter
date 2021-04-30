package hu.bme.aut.android.puppysitter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.appbar.AppBarLayout
import hu.bme.aut.android.puppysitter.databinding.ActivityMatcherBinding
import java.util.*


class MatcherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMatcherBinding
    private lateinit var adapter: ViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatcherBinding.inflate(layoutInflater)
        val images: MutableList<Bitmap> = mutableListOf<Bitmap>()
        images.add(BitmapFactory.decodeStream(resources.openRawResource(R.raw.penny1)))
        images.add(BitmapFactory.decodeStream(resources.openRawResource(R.raw.nyonya)))
        images.add(BitmapFactory.decodeStream(resources.openRawResource(R.raw.szemcso2)))
        adapter = ViewPagerAdapter(layoutInflater, images)
        binding.pager.adapter = adapter
        binding.appbar.addOnOffsetChangedListener(MyOffsetChangedListener())
        binding.btnDown.setOnClickListener {
            binding.appbar.setExpanded(true)
        }
        setContentView(binding.root)
    }

    inner class MyOffsetChangedListener: AppBarLayout.BaseOnOffsetChangedListener<AppBarLayout>{
        override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
            if(verticalOffset < 0){
                binding.btnDown.visibility = View.VISIBLE
                binding.btnDown.isClickable = true
            } else {
                binding.btnDown.visibility = View.INVISIBLE
                binding.btnDown.isClickable = false
            }
        }

    }

    override fun onBackPressed() {
        if(binding.appbar.isLifted){
            binding.appbar.setExpanded(true)
        } else {
            super.onBackPressed()
        }
    }

    inner class ViewPagerAdapter(private val mLayoutInflater: LayoutInflater, private val images: MutableList<Bitmap>): PagerAdapter(){
        override fun getCount(): Int = images.size

        override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj as LinearLayout

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            // inflating the item.xml
            val itemView: View = mLayoutInflater.inflate(R.layout.viewpager_item, container, false)

            // referencing the image view from the item.xml file
            val imageView: ImageView = itemView.findViewById<View>(R.id.imageViewMain) as ImageView

            imageView.setOnClickListener {
                binding.appbar.setExpanded(false)
            }

            // setting the image in the imageView
            imageView.setImageBitmap(images[position])

            // Adding the View
            Objects.requireNonNull(container).addView(itemView)
            return itemView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as LinearLayout)
        }

    }
}