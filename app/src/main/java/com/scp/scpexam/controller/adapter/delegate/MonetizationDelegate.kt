package com.scp.scpexam.controller.adapter.delegate

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import com.scp.scpexam.BuildConfig
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.viewmodel.MonetizationViewModel


class MonetizationDelegate(
    private val clickListener: (String) -> Unit
) : AbsListItemAdapterDelegate<MonetizationViewModel, MyListItem, MonetizationDelegate.ViewHolder>() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) =
        item is MonetizationViewModel

    override fun onCreateViewHolder(parent: ViewGroup) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_monetization,
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        item: MonetizationViewModel,
        viewHolder: ViewHolder,
        payloads: MutableList<Any>
    ) {
        with(viewHolder) {
            iconImageView.setImageResource(item.icon)
            titleTextView.text = item.title
            descriptionTextView.text = item.description

            if (item.isAlreadyOwned) {
                alreadyOwnedTextView.visibility = VISIBLE
                @Suppress("ConstantConditionIf")
                if (BuildConfig.DEBUG) {
                    alreadyOwnedTextView.setOnClickListener { clickListener.invoke(item.sku!!) }
                }
            } else {
                alreadyOwnedTextView.visibility = GONE
                alreadyOwnedTextView.setOnClickListener(null)

                itemView.setOnClickListener { item.action.invoke(item.sku) }
            }

            priceTextView.text = item.price
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView
        val titleTextView: TextView
        val descriptionTextView: TextView
        val alreadyOwnedTextView: TextView
        val priceTextView: TextView

        init {
            iconImageView = itemView.findViewById(R.id.iconImageView)
            titleTextView = itemView.findViewById(R.id.titleTextView)
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView)
            alreadyOwnedTextView = itemView.findViewById(R.id.alreadyOwnedTextView)
            priceTextView = itemView.findViewById(R.id.priceTextView)
        }

    }
}