package ru.kuchanov.scpquiz.controller.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_monetization.view.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.MonetizationViewModel


class MonetizationDelegate : AbsListItemAdapterDelegate<
        MonetizationViewModel,
        MyListItem,
        MonetizationDelegate.ViewHolder
        >() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) =
            item is MonetizationViewModel

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_monetization,
            parent,
            false
        )
    )

    override fun onBindViewHolder(item: MonetizationViewModel, viewHolder: ViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView) {
            iconImageView.setImageResource(item.icon)
            titleTextView.text = item.title
            descriptionTextView.text = item.description
            alreadyOwnedTextView.visibility = if (item.isAlreadyOwned) VISIBLE else GONE
            setOnClickListener { item.action.invoke(Unit) }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}