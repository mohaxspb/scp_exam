package com.scp.scpexam.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.google.android.flexbox.FlexboxLayout
import com.scp.scpexam.R
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.databinding.ViewKeyboardBinding
import com.scp.scpexam.di.Di
import com.scp.scpexam.utils.DimensionUtils
import com.scp.scpexam.utils.SystemUtils
import timber.log.Timber
import toothpick.Toothpick
import javax.inject.Inject


@SuppressWarnings("Injectable")
class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        val PADDING_TOP_BOTTOM = DimensionUtils.dpToPx(16)
        val PADDING_LEFT = DimensionUtils.dpToPx(16)
        private const val MIN_KEY_COUNT = 21

        fun fillCharsList(chars: MutableList<Char>, availableChars: List<Char>): MutableList<Char> {
            if (chars.size < KeyboardView.MIN_KEY_COUNT) {
                val charsToAddCount = KeyboardView.MIN_KEY_COUNT - chars.size

                Timber.d("chars: $chars")
                Timber.d("availableChars: $availableChars")
                val topBorder =
                    if (availableChars.size > charsToAddCount) charsToAddCount else availableChars.size
                chars.addAll(availableChars.subList(0, topBorder))
                chars.shuffle()
                Timber.d("chars.size: ${chars.size}")
            }
            return when {
                chars.size < KeyboardView.MIN_KEY_COUNT -> fillCharsList(chars, availableChars)
                else -> chars
            }
        }
    }

    private lateinit var binding: ViewKeyboardBinding

    @Inject
    lateinit var myPreferenceManager: MyPreferenceManager

    private var characters = mutableListOf<Char>()

    private val characterViewsMap = mutableMapOf<Int, CharacterView>()

    var keyPressListener: (Char, Int) -> Unit = { _, _ -> }

    init {
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))

        orientation = VERTICAL
        inflate(context, R.layout.view_keyboard, this)
        setPadding(PADDING_LEFT, PADDING_TOP_BOTTOM, PADDING_LEFT, PADDING_TOP_BOTTOM)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = ViewKeyboardBinding.inflate(LayoutInflater.from(context), this)
    }

    fun removeCharView(charId: Int) = characterViewsMap[charId]?.let {
        Timber.d("removeCharView: $charId")
        it.visibility = View.INVISIBLE
        it.isEnabled = false
    }

    private fun addCharView(char: Char) {
        val characterView = CharacterView(context)
        characterView.char = char

        binding.flexBoxLayout.addView(characterView)
        characterView.charId = binding.flexBoxLayout.childCount - 1
        characterViewsMap[characterView.charId] = characterView

        characterView.setOnClickListener {
            if (myPreferenceManager.isVibrationEnabled()) {
                SystemUtils.vibrate()
            }
            with(it as CharacterView) {
                Timber.d("keyPressListener: $char/$charId")
                keyPressListener(it.char, it.charId)
            }
        }

        val params = characterView.layoutParams as FlexboxLayout.LayoutParams
        params.flexBasisPercent = .3f
        characterView.layoutParams = params
    }

    fun setCharacters(characters: List<Char>) {
        Timber.d("setCharacters: $characters")
        this.characters.clear()
        this.characters.addAll(characters.shuffled())
        binding.flexBoxLayout.removeAllViews()
        characterViewsMap.clear()
        this.characters.forEach { addCharView(it) }
    }

    fun restoreChar(charId: Int) = characterViewsMap[charId]?.apply {
        visibility = View.VISIBLE
        isEnabled = true
    }
}