package ru.kuchanov.scpquiz.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.view_keyboard.view.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.utils.DimensionUtils

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var characters = listOf<Char>()

    init {
        //to do?..
        orientation = VERTICAL
        inflate(context, R.layout.view_keyboard, this)
        val paddingTopAndLeft = DimensionUtils.dpToPx(24)
        setPadding(paddingTopAndLeft, paddingTopAndLeft, 0, 0)

        //fixme test
        setCharacters(listOf('a', 'b', 'c', 'd', 'e'))
    }

    fun setCharacters(characters: List<Char>) {
        val charsCount = characters.size

        characters.forEach {
            val characterView = CharacterView(context)
            characterView.char = it
            topLineView.addView(characterView)

//            val characterView1 = CharacterView(context)
//            characterView1.char = it
//            middleLineView.addView(characterView1)
//
//            val characterView2 = CharacterView(context)
//            characterView2.char = it
//            bottomLineView.addView(characterView2)

            characterView.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            characterView.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
        }

        characters.forEach {
            val characterView = TextView(context)
            characterView.text = it.toString()
            topLineView.addView(characterView)

//            val characterView1 = TextView(context)
//            characterView1.text = it.toString()
//            middleLineView.addView(characterView1)
//
//            val characterView2 = TextView(context)
//            characterView2.text = it.toString()
//            bottomLineView.addView(characterView2)

            characterView.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            characterView.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
        }
    }
}