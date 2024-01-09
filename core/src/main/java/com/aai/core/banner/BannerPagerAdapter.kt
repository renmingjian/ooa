package com.aai.core.banner

import android.content.Context
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.aai.core.banner.CustomBanner.OnPageClickListener
import com.aai.core.banner.CustomBanner.ViewCreator

class BannerPagerAdapter<T>(
    private val mContext: Context,
    private val mCreator: ViewCreator<T>?,
    private val mData: List<T>?
) : PagerAdapter() {
    private var mOnPageClickListener: OnPageClickListener<T>? = null
    private val views = SparseArray<View>()
    override fun getCount(): Int {
        return if (mData.isNullOrEmpty()) 0 else if (mData.size == 1) 1 else mData.size + 2
    }

    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        return arg0 === arg1
    }

    override fun destroyItem(
        container: ViewGroup, position: Int,
        `object`: Any
    ) {
        //Warning：不要在这里调用removeView
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val item = getActualPosition(position)
        var view = views[position]
        if (view == null) {
            view = mCreator?.createView(mContext, item)!!
            views.put(position, view)
        }

        //如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
        val vp = view.parent
        if (vp != null) {
            val parent = vp as ViewGroup
            parent.removeView(view)
        }
        val t = mData!![item]
        container.addView(view)
        mCreator?.updateUI(mContext, view, item, t)
        view.setOnClickListener {
            if (mOnPageClickListener != null) {
                mOnPageClickListener!!.onPageClick(item, t)
            }
        }

        return view
    }

    private fun getActualPosition(position: Int): Int {
        return when (position) {
            0 -> mData!!.size - 1
            count - 1 -> 0
            else -> position - 1
        }
    }

    fun setOnPageClickListener(l: OnPageClickListener<T>?) {
        mOnPageClickListener = l
    }
}