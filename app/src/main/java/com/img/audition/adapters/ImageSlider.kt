package com.img.audition.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.img.audition.R
import com.img.audition.databinding.ItemImageSliderBinding


class ImageSlider(private val context: Context, private val imageList: ArrayList<Int>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater =  context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView =  inflater.inflate(R.layout.item_image_slider,null)
        val slideImage: ImageView = itemView.findViewById(R.id.slideImageView)

        slideImage.setImageResource(imageList[position])
        container.addView(itemView as View)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return imageList.size
    }
}