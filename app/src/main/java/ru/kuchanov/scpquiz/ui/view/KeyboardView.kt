package ru.kuchanov.scpquiz.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.view_keyboard.view.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.utils.DimensionUtils


class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        val PADDING_TOP_BOTTOM = DimensionUtils.dpToPx(16)
        val PADDING_LEFT = DimensionUtils.dpToPx(16)
        const val MIN_KEY_COUNT = 21
    }

    private var characters = mutableListOf<Char>()

    var keyPressListener: (Char, CharacterView) -> Unit = { _, _ -> }

    init {
        orientation = VERTICAL
        inflate(context, R.layout.view_keyboard, this)
        setPadding(PADDING_LEFT, PADDING_TOP_BOTTOM, PADDING_LEFT, PADDING_TOP_BOTTOM)
    }

    fun removeCharView(view: CharacterView) {
        flexBoxLayout.removeView(view)
        characters
    }

    fun addCharView(char: Char) {
        val characterView = CharacterView(context)
        characterView.char = char

        characterView.setOnClickListener { keyPressListener((it as CharacterView).char, it) }

        flexBoxLayout.addView(characterView)

//        val marginParams = characterView.layoutParams as ViewGroup.MarginLayoutParams
////        marginParams.marginEnd = resources.getDimensionPixelSize(R.dimen.defaultMargin)
////        marginParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.defaultMargin)
//        marginParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
//        marginParams.height = LinearLayout.LayoutParams.MATCH_PARENT

        val params = characterView.layoutParams as FlexboxLayout.LayoutParams
//        marginParams.marginEnd = resources.getDimensionPixelSize(R.dimen.defaultMargin)
//        marginParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.defaultMargin)
//        params.width = LinearLayout.LayoutParams.WRAP_CONTENT
//        params.height = LinearLayout.LayoutParams.MATCH_PARENT

        params.flexBasisPercent = .3f

        characterView.layoutParams = params
    }

    fun setCharacters(characters: List<Char>) {
        this.characters.clear()
        this.characters.addAll(characters)
        characters.forEach { addCharView(it) }
    }
}