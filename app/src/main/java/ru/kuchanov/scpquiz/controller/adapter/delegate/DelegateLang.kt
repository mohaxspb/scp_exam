package ru.kuchanov.scpquiz.controller.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_lang.view.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LangViewModel
import ru.kuchanov.scpquiz.ui.fragment.ScpSettingsFragment

class DelegateLang(
    private val clickListener: (String) -> Unit
) : AbsListItemAdapterDelegate<LangViewModel, MyListItem, DelegateLang.LangViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup): LangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_lang,
            parent,
            false
        )
        return LangViewHolder(view)
    }

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) = item is LangViewModel

    override fun onBindViewHolder(item: LangViewModel,
        viewHolder: LangViewHolder,
        payloads: MutableList<Any>
    ) = with(viewHolder.itemView) {
        languageImageView.setImageResource(ScpSettingsFragment.getIconForLang(item.lang))
        languageLabelTextView.text = item.lang
        setBackgroundResource(if (item.selected) android.R.color.white else R.color.bg_grey_transparent)
        setOnClickListener { clickListener(item.lang) }
    }


    class LangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
