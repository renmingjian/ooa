package com.aai.core.banner

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.aai.core.R
import com.aai.core.utils.dpToPx
import com.aai.core.utils.getDrawableWithColor
import com.aai.core.utils.toPx

/**
 *
 * @param <T>
</T> */
class CustomBanner<T : Any> : FrameLayout {
    private var mContext: Context? = null
    private var mBannerViewPager: ViewPager? = null

    //普通指示器的容器
    private var mIndicatorLayout: LinearLayout? = null
    private var mAdapter: BannerPagerAdapter<T>? = null
    private var mScroller: ViewPagerScroller? = null

    /**
     * 获取轮播间隔时间
     *
     * @return
     */
    var intervalTime: Long = 0
        private set
    private var mIndicatorSelectDrawable: Drawable? = null
    private var mIndicatorUnSelectDrawable: Drawable? = null
    private var mIndicatorInterval = 0
    var indicatorGravity = IndicatorGravity.CENTER
        private set
    private var mIndicatorStyle = IndicatorStyle.ORDINARY
    private var mBannerCount = 0

    /**
     * 是否轮播
     *
     * @return
     */
    var isTurning = false
        private set
    private var mOnPageClickListener: OnPageClickListener<T>? = null
    private var mOnPageChangeListener: OnPageChangeListener? = null
    private val mTimeHandler = Handler()
    private val mTurningTask: Runnable = object : Runnable {
        override fun run() {
            if (mBannerCount > 1) {
                if (isTurning && mBannerViewPager != null) {
                    val page = mBannerViewPager!!.currentItem + 1
                    mBannerViewPager!!.currentItem = page
                    mTimeHandler.postDelayed(this, intervalTime)
                }
            }
        }
    }

    /**
     * 指示器方向
     */
    enum class IndicatorGravity {
        LEFT, RIGHT, CENTER
    }

    /**
     * 指示器类型
     */
    enum class IndicatorStyle {
        NONE,
        ORDINARY
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
        getAttrs(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
        getAttrs(context, attrs)
    }

    private fun getAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.custom_banner)
            when (mTypedArray.getInt(R.styleable.custom_banner_indicatorGravity, 3)) {
                1 -> indicatorGravity = IndicatorGravity.LEFT
                2 -> indicatorGravity = IndicatorGravity.RIGHT
                3 -> indicatorGravity = IndicatorGravity.CENTER
            }
            val style = mTypedArray.getInt(R.styleable.custom_banner_indicatorStyle, 3)
            if (style == 1) {
                mIndicatorStyle = IndicatorStyle.NONE
            } else if (style == 3) {
                mIndicatorStyle = IndicatorStyle.ORDINARY
            }
            mIndicatorInterval = mTypedArray.getDimensionPixelOffset(
                R.styleable.custom_banner_indicatorInterval, 5.toPx()
            )
            val indicatorSelectRes = mTypedArray.getResourceId(
                R.styleable.custom_banner_indicatorSelectRes, 0
            )
            val indicatorUnSelectRes = mTypedArray.getResourceId(
                R.styleable.custom_banner_indicatorUnSelectRes, 0
            )
            if (indicatorSelectRes != 0) {
                mIndicatorSelectDrawable = context.resources.getDrawable(indicatorSelectRes)
            }
            if (indicatorUnSelectRes != 0) {
                mIndicatorUnSelectDrawable = context.resources.getDrawable(indicatorUnSelectRes)
            }
            mTypedArray.recycle()
        }
    }

    private fun init(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_banner, this, true)
        mBannerViewPager = view.findViewById(R.id.bannerViewPager)
        mIndicatorLayout = view.findViewById(R.id.llIndicator)
        mContext = context
        addBannerViewPager()
        addIndicatorLayout()
    }

    private fun addBannerViewPager() {
        mBannerViewPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetP: Int
            ) {
                if (!isMarginal(position) && mOnPageChangeListener != null) {
                    mOnPageChangeListener!!.onPageScrolled(
                        getActualPosition(position),
                        positionOffset, positionOffsetP
                    )
                }
            }

            override fun onPageSelected(position: Int) {
                if (!isMarginal(position) && mOnPageChangeListener != null) {
                    mOnPageChangeListener!!.onPageSelected(getActualPosition(position))
                }
                updateIndicator()
            }

            override fun onPageScrollStateChanged(state: Int) {
                val position = mBannerViewPager!!.currentItem
                if (!isMarginal(position) && mOnPageChangeListener != null) {
                    mOnPageChangeListener!!.onPageScrollStateChanged(state)
                }
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (position == 0) {
                        mScroller!!.isSudden = true
                        mBannerViewPager!!.setCurrentItem(mAdapter!!.count - 2, true)
                        mScroller!!.isSudden = false
                    } else if (position == mAdapter!!.count - 1) {
                        mScroller!!.isSudden = true
                        mBannerViewPager!!.setCurrentItem(1, true)
                        mScroller!!.isSudden = false
                    }
                }
            }
        })
        replaceViewPagerScroll()
    }

    private fun addIndicatorLayout() {
        //添加普通指示器容器
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        lp.gravity = analysisGravity(indicatorGravity)
        mIndicatorLayout!!.gravity = Gravity.CENTER
        mIndicatorLayout!!.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        mIndicatorLayout!!.dividerDrawable = getDividerDrawable(mIndicatorInterval)
        mIndicatorLayout!!.visibility =
            if (mIndicatorStyle == IndicatorStyle.ORDINARY) VISIBLE else GONE
    }

    private fun getDividerDrawable(interval: Int): Drawable {
        val drawable = ShapeDrawable()
        drawable.paint.color = Color.TRANSPARENT
        drawable.intrinsicWidth = interval
        return drawable
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (isTurning) {
            if (hasWindowFocus) {
                startTurning(intervalTime)
            } else {
                stopTurning()
                isTurning = true
            }
        }
    }

    private fun isMarginal(position: Int): Boolean {
        return position == 0 || position == count + 1
    }

    /**
     * 设置轮播图数据
     *
     * @param creator 创建和更新轮播图View的接口
     * @param data    轮播图数据
     * @return
     */
    fun setPages(creator: ViewCreator<T>?, data: List<T>?): CustomBanner<T> {
        mAdapter = BannerPagerAdapter(context, creator, data)
        if (mOnPageClickListener != null) {
            mAdapter!!.setOnPageClickListener(mOnPageClickListener)
        }
        mBannerViewPager!!.adapter = mAdapter
        if (data == null) {
            mIndicatorLayout!!.removeAllViews()
            mBannerCount = 0
        } else {
            mBannerCount = data.size
            initIndicator(data.size)
        }
        setCurrentItem(0)
        updateIndicator()
        return this
    }

    /**
     * 设置指示器资源
     *
     * @param selectRes   选中的效果资源
     * @param unSelectRes 未选中的效果资源
     * @return
     */
    fun setIndicatorRes(selectRes: Int, unSelectRes: Int): CustomBanner<T> {
        mIndicatorSelectDrawable = mContext!!.resources.getDrawable(selectRes)
        mIndicatorUnSelectDrawable = mContext!!.resources.getDrawable(unSelectRes)
        updateIndicator()
        return this
    }

    /**
     * 设置指示器资源
     *
     * @param select   选中的效果资源
     * @param unSelect 未选中的效果资源
     * @return
     */
    fun setIndicator(select: Drawable?, unSelect: Drawable?): CustomBanner<T> {
        mIndicatorSelectDrawable = select
        mIndicatorUnSelectDrawable = unSelect
        updateIndicator()
        return this
    }

    /**
     * 设置指示器方向
     *
     * @param gravity 指示器方向 左、中、右三种
     * @return
     */
    fun setIndicatorGravity(gravity: IndicatorGravity): CustomBanner<T> {
        if (indicatorGravity != gravity) {
            indicatorGravity = gravity
            setOrdinaryIndicatorGravity(gravity)
        }
        return this
    }

    private fun setOrdinaryIndicatorGravity(gravity: IndicatorGravity) {
        val lp = mIndicatorLayout!!.layoutParams as LayoutParams
        lp.gravity = analysisGravity(gravity)
        mIndicatorLayout!!.layoutParams = lp
    }

    /**
     * 设置指示器类型
     *
     * @param style 指示器类型 普通指示器 数字指示器 没有指示器 三种
     * @return
     */
    fun setIndicatorStyle(style: IndicatorStyle): CustomBanner<T> {
        if (mIndicatorStyle != style) {
            mIndicatorStyle = style
            mIndicatorLayout!!.visibility =
                if (mIndicatorStyle == IndicatorStyle.ORDINARY) VISIBLE else GONE
            updateIndicator()
        }
        return this
    }

    /**
     * 设置指示器间隔
     *
     * @param interval
     * @return
     */
    fun setIndicatorInterval(interval: Int): CustomBanner<T> {
        if (mIndicatorInterval != interval) {
            mIndicatorInterval = interval
            mIndicatorLayout!!.dividerDrawable = getDividerDrawable(interval)
        }
        return this
    }

    private fun analysisGravity(gravity: IndicatorGravity): Int {
        return when (gravity) {
            IndicatorGravity.LEFT -> {
                Gravity.BOTTOM or Gravity.START
            }
            IndicatorGravity.RIGHT -> {
                Gravity.BOTTOM or Gravity.END
            }
            else -> {
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }
        }
    }

    /**
     * 启动轮播
     *
     * @param intervalTime 轮播间隔时间
     * @return
     */
    fun startTurning(intervalTime: Long): CustomBanner<T> {
        if (isTurning) {
            stopTurning()
        }
        isTurning = true
        this.intervalTime = intervalTime
        if (mBannerCount > 1) {
            mTimeHandler.postDelayed(mTurningTask, this.intervalTime)
        }
        return this
    }

    /**
     * 停止轮播
     *
     * @return
     */
    fun stopTurning(): CustomBanner<T> {
        isTurning = false
        mTimeHandler.removeCallbacks(mTurningTask)
        return this
    }

    val count: Int
        get() = if (mAdapter == null || mAdapter!!.count == 0) {
            0
        } else mAdapter!!.count - 2

    fun setCurrentItem(position: Int): CustomBanner<*> {
        if (position >= 0 && position < mAdapter!!.count) {
            mBannerViewPager!!.currentItem = position + 1
        }
        return this
    }

    val currentItem: Int
        get() = getActualPosition(mBannerViewPager!!.currentItem)

    private fun getActualPosition(position: Int): Int {
        if (mAdapter == null || mAdapter!!.count == 0) {
            return -1
        }
        return when (position) {
            0 -> {
                count - 1
            }
            count + 1 -> {
                0
            }
            else -> {
                position - 1
            }
        }
    }

    private fun initIndicator(count: Int) {
        mIndicatorLayout!!.removeAllViews()
        if (count > 0) {
            for (i in 0 until count) {
                val imageView = ImageView(mContext)
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                lp.rightMargin = 4.toPx()
                lp.width = 8.toPx()
                lp.height = 8.toPx()
                mIndicatorLayout!!.addView(imageView, lp)
            }
        }
    }

    /**
     * 更新指示器
     */
    private fun updateIndicator() {
        if (mIndicatorStyle == IndicatorStyle.ORDINARY) {
            val count = mIndicatorLayout!!.childCount
            val currentPage = currentItem
            if (count > 0) {
                for (i in 0 until count) {
                    val view = mIndicatorLayout!!.getChildAt(i) as ImageView
                    if (i == currentPage) {
                        view.setImageDrawable(
                            getDrawableWithColor(context, R.drawable.shape_point_select)
                                ?: mIndicatorSelectDrawable
                        )
                    } else {
                        view.setImageDrawable(mIndicatorUnSelectDrawable)
                    }
                }
            }
        }
    }

    /**
     * 通过反射替换掉mBannerViewPager的mScroller属性。
     * 这样做是为了改变和控件ViewPager的滚动速度。
     */
    private fun replaceViewPagerScroll() {
        try {
            val field = ViewPager::class.java.getDeclaredField("mScroller")
            field.isAccessible = true
            mScroller = ViewPagerScroller(
                mContext,
                AccelerateInterpolator()
            )
            field[mBannerViewPager] = mScroller
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置轮播图的滚动速度
     *
     * @param scrollDuration
     */
    fun setScrollDuration(scrollDuration: Int): CustomBanner<T> {
        mScroller!!.scrollDuration = scrollDuration
        return this
    }

    val scrollDuration: Int
        get() = mScroller!!.scrollDuration

    fun setOnPageChangeListener(l: OnPageChangeListener?): CustomBanner<*> {
        mOnPageChangeListener = l
        return this
    }

    fun setOnPageClickListener(l: OnPageClickListener<T>?): CustomBanner<T> {
        if (mAdapter != null) {
            mAdapter!!.setOnPageClickListener(l)
        }
        mOnPageClickListener = l
        return this
    }

    fun setBannerSize(height: Int) {
        val layoutParams = mBannerViewPager!!.layoutParams as LinearLayout.LayoutParams
        layoutParams.height = dpToPx(height.toFloat())
        mBannerViewPager!!.layoutParams = layoutParams
    }

    fun showIndicators(show: Boolean) {
        mIndicatorLayout?.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun showIndicatorsAccordingSize(imageSize: Int) {
        showIndicators(imageSize > 1)
    }

    /**
     * 通知数据刷新
     */
    fun notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter!!.notifyDataSetChanged()
        }
    }

    interface OnPageClickListener<T> {
        fun onPageClick(position: Int, t: T)
    }

    /**
     * 创建和更新轮播图View的接口
     *
     * @param <T>
    </T> */
    interface ViewCreator<T> {
        fun createView(context: Context?, position: Int): View?
        fun updateUI(context: Context?, view: View?, position: Int, t: T)
    }
}