package ru.kuchanov.scpquiz.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.widget.TextView
import ru.kuchanov.scpquiz.R

class CharacterView : TextView {

    var char: Char = ' '
        set(value) {
            field = value
            text = field.toString()
        }

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec)
    }
}