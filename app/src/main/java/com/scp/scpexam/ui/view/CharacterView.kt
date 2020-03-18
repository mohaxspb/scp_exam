package com.scp.scpexam.ui.view

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import com.scp.scpexam.R

/**
 * extend AppCompatTextView for apply custom font from xml
 *
 * see [https://stackoverflow.com/a/48177460/3212712]
 */
class CharacterView : AppCompatTextView {

    var char: Char = ' '
        set(value) {
            field = value
            text = field.toString()
        }

    var charId: Int = NO_ID

    var isSquare = true

    constructor(context: Context) : this(context, null, R.style.CharacterViewStyle)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.style.CharacterViewStyle)

    /**
     * use ContextThemeWrapper to apply style
     *
     * see [https://stackoverflow.com/a/28613069/3212712]
     */
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        ContextThemeWrapper(context, defStyleAttr),
        attrs,
        0
    )

    init {
        text = char.toString()
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (isSquare) {
            val height = measuredHeight
            val width = measuredWidth
            val size = Math.max(height, width)

            //from https://stackoverflow.com/a/39727439/3212712
            val widthSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
            super.onMeasure(widthSpec, heightSpec)
        }
    }
}