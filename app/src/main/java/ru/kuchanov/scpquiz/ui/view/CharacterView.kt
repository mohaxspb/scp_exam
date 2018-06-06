package ru.kuchanov.scpquiz.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import ru.kuchanov.scpquiz.R
import timber.log.Timber

class CharacterView : TextView {

    //    var char: Char = "".toCharArray()[0]
    var char: Char = ' '
        set(value) {
            field = value
//            text = char.toString()
            text = field.toString()
        }
//        get() = text[0]

    constructor(context: Context) : this(context, null, R.style.CharacterViewStyle)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.style.CharacterViewStyle)

//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * see https://stackoverflow.com/a/28613069/3212712
     */
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        ContextThemeWrapper(context, R.style.CharacterViewStyle),
        attrs,
        0
    )


    init {
        text = char.toString()

        setOnClickListener { Timber.d("CLicked!!!") }
    }

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
//        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
//        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
//        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
//
//        val size: Int
//        if (widthMode == View.MeasureSpec.EXACTLY && widthSize > 0) {
//            size = widthSize
//        } else if (heightMode == View.MeasureSpec.EXACTLY && heightSize > 0) {
//            size = heightSize
//        } else {
//            size = if (widthSize < heightSize) widthSize else heightSize
//        }
//
//        val finalMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
//        super.onMeasure(finalMeasureSpec, finalMeasureSpec)
//    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        val size = Math.min(widthMeasureSpec, heightMeasureSpec)
//        super.onMeasure(size, size)

//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        val size = Math.min(measuredWidth, measuredHeight)
//        setMeasuredDimension(size, size);

        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }
}