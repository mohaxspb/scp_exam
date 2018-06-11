package ru.kuchanov.scpquiz.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.view_keyboard.view.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.utils.DimensionUtils
import timber.log.Timber


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

        val params = characterView.layoutParams as FlexboxLayout.LayoutParams
        params.flexBasisPercent = .3f
        characterView.layoutParams = params
    }

    fun setCharacters(characters: List<Char>) {
        Timber.d("setCharacters: $characters")
        this.characters.clear()
        this.characters.addAll(characters)
        flexBoxLayout.removeAllViews()
        this.characters.forEach { addCharView(it) }
    }
}