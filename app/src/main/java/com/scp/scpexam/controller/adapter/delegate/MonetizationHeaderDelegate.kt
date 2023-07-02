package com.scp.scpexam.controller.adapter.delegate

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.viewmodel.MonetizationHeaderViewModel

class MonetizationHeaderDelegate(
    private val onSocialProviderClickListener: (Constants.Social) -> Unit
) : AbsListItemAdapterDelegate<
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

    override fun onBindViewHolder(
        item: MonetizationHeaderViewModel,
        viewHolder: ViewHolder,
        payloads: MutableList<Any>
    ) {
        with(viewHolder) {
            coinsLabelTextView.text =
                itemView.context.getString(R.string.coins_amount, item.player.score)
            authButtons.visibility = if (item.showAuthButtons) VISIBLE else GONE
            vkImage.setOnClickListener { onSocialProviderClickListener(Constants.Social.VK) }
            faceBookImage.setOnClickListener { onSocialProviderClickListener(Constants.Social.FACEBOOK) }
            googleImage.setOnClickListener { onSocialProviderClickListener(Constants.Social.GOOGLE) }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authButtons: View
        val coinsLabelTextView: TextView
        val vkImage: View
        val faceBookImage: View
        val googleImage: View

        init {
            authButtons = itemView.findViewById(R.id.authButtons)
            coinsLabelTextView = itemView.findViewById(R.id.coinsLabelTextView)
            vkImage = itemView.findViewById(R.id.vkImage)
            faceBookImage = itemView.findViewById(R.id.faceBookImage)
            googleImage = itemView.findViewById(R.id.googleImage)
        }
    }
}