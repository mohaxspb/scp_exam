package com.scp.scpexam.controller.adapter.delegate

import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.viewmodel.LangViewModel
import kotlinx.android.synthetic.main.list_item_lang.view.*
import com.scp.scpexam.R
import com.scp.scpexam.utils.LocaleUtils

class DelegateLang(private val clickListener: (String) -> Unit)
    : AbsListItemAdapterDelegate<LangViewModel, MyListItem, DelegateLang.LangViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup): LangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_lang,
                parent,
                false
        )
        return LangViewHolder(view)
    }

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) =
            item is LangViewModel

    override fun onBindViewHolder(
            item: LangViewModel,
            viewHolder: LangViewHolder,
            payloads: MutableList<Any>
    ) = with(viewHolder.itemView) {
        languageLabelTextView.text = item.lang
        languageImageView.countryCode = LocaleUtils.countryCodeFromLocale(item.lang)
        languageLabelTextView.setTextColor(
                ContextCompat.getColor(
                        context,
                        if (!item.selected) android.R.color.white else android.R.color.black)
        )

        setBackgroundResource(if (item.selected) android.R.color.white else R.color.bg_grey_transparent)
        setOnClickListener { clickListener(item.lang) }
    }


    class LangViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}
