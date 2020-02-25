package com.scp.scpexam.controller.adapter.delegate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_monetization_header.view.*
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.viewmodel.MonetizationHeaderViewModel


class MonetizationHeaderDelegate : AbsListItemAdapterDelegate<
        MonetizationHeaderViewModel,
        MyListItem,
        MonetizationHeaderDelegate.ViewHolder
        >() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) =
            item is MonetizationHeaderViewModel

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_monetization_header,
            parent,
            false
        )
    )

    override fun onBindViewHolder(item: MonetizationHeaderViewModel, viewHolder: ViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView) {
            coinsLabelTextView.text = context.getString(R.string.coins_amount, item.player.score)
        }
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}