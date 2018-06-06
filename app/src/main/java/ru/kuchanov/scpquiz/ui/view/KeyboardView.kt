package ru.kuchanov.scpquiz.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_keyboard.view.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.utils.DimensionUtils

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        val PADDING_TOP_LEFT = DimensionUtils.dpToPx(24)
    }

    private var characters = listOf<Char>()

    init {
        //to do?..
        orientation = VERTICAL
        inflate(context, R.layout.view_keyboard, this)
        setPadding(PADDING_TOP_LEFT, PADDING_TOP_LEFT, 0, 0)

        //fixme test
        setCharacters(listOf('a', 'b', 'c', 'd', 'e'))
    }

    fun setCharacters(characters: List<Char>) {
        val charsCount = characters.size
        //todo use https://github.com/google/flexbox-layout
        characters.forEach {
            val characterView = CharacterView(context)
            characterView.char = it
            topLineView.addView(characterView)

            characterView.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            characterView.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
        }
        //todo use https://github.com/google/flexbox-layout
        characters.forEach {
            val characterView = CharacterView(context)
            characterView.char = it
            middleLineView.addView(characterView)

            characterView.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            characterView.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
        }
        //todo use https://github.com/google/flexbox-layout
        characters.forEach {
            val characterView = CharacterView(context)
            characterView.char = it
            bottomLineView.addView(characterView)

            characterView.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            characterView.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
        }
    }
}