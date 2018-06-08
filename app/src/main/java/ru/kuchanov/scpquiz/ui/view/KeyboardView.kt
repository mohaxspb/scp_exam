package ru.kuchanov.scpquiz.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
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

    var keyPressListener: (Char) -> Unit = {}

    init {
        //to do?..
        orientation = VERTICAL
        inflate(context, R.layout.view_keyboard, this)
        setPadding(PADDING_TOP_LEFT, PADDING_TOP_LEFT, 0, 0)

        //fixme test
        val chars = mutableListOf('a', 'b', 'c', 'd', 'e')
        chars.addAll(listOf('a', 'b', 'c', 'd', 'e'))
        chars.addAll(listOf('a', 'b', 'c', 'd', 'e'))
        chars.addAll(listOf('a', 'b', 'c', 'd', 'e'))
        chars.addAll(listOf('a', 'b', 'c', 'd', 'e'))
        setCharacters(chars)
    }

    fun setCharacters(characters: List<Char>) {
        val charsCount = characters.size
        characters.forEach {
            val characterView = CharacterView(context)
            characterView.char = it

            characterView.setOnClickListener { keyPressListener((it as CharacterView).char) }

            flexBoxLayout.addView(characterView)

            val marginParams = characterView.layoutParams as ViewGroup.MarginLayoutParams
            marginParams.marginEnd = resources.getDimensionPixelSize(R.dimen.defaultMargin)
            marginParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.defaultMargin)
            marginParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            marginParams.height = LinearLayout.LayoutParams.MATCH_PARENT

            characterView.layoutParams = marginParams
        }
    }
}