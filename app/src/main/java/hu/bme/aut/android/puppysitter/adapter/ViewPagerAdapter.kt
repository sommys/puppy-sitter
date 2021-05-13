package hu.bme.aut.android.puppysitter.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.appbar.AppBarLayout
import hu.bme.aut.android.puppysitter.R
import java.util.*

class ViewPagerAdapter(private val mLayoutInflater: LayoutInflater, private val images: MutableList<Bitmap>, private val appbar: AppBarLayout): PagerAdapter(){
    override fun getCount(): Int = images.size

    override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj as LinearLayout

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // inflating the item.xml
        val itemView: View = mLayoutInflater.inflate(R.layout.viewpager_item, container, false)

        // referencing the image view from the item.xml file
        val imageView: ImageView = itemView.findViewById<View>(R.id.imageViewMain) as ImageView

        imageView.setOnClickListener {
            appbar.setExpanded(false)
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