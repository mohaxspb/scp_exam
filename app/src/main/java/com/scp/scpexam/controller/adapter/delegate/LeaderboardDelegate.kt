package com.scp.scpexam.controller.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.viewmodel.LeaderboardViewModel
import com.scp.scpexam.databinding.ItemLeaderboardBinding
import com.scp.scpexam.ui.utils.GlideApp

class LeaderboardDelegate :
    AbsListItemAdapterDelegate<LeaderboardViewModel, MyListItem, LeaderboardDelegate.LeaderboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup): LeaderboardViewHolder =
        LeaderboardViewHolder(
            ItemLeaderboardBinding.inflate(
                LayoutInflater.from(parent.context), null, false
            )
        )

    override fun isForViewType(
        item: MyListItem,
        items: MutableList<MyListItem>,
        position: Int
    ): Boolean = item is LeaderboardViewModel

    override fun onBindViewHolder(
        item: LeaderboardViewModel,
        viewHolder: LeaderboardViewHolder,
        payloads: MutableList<Any>
    ) {
        with(viewHolder.binding) {
            userFullNameTextView.text = item.name
            userScoreTextView.text = item.score.toString()
            userPositionTextView.text = (viewHolder.adapterPosition + 1).toString()
            userFullCompleteLevelsTextView.text =
                root.context.getString(R.string.complete_levels_text, item.fullCompleteLevels)
            userPartCompleteLevelsTextView.text =
                root.context.getString(R.string.part_complete_levels_text, item.partCompleteLevels)

            val glideRequest = GlideApp.with(userAvatarImageView.context)
            when (item.avatarUrl) {
                null -> glideRequest.load(R.drawable.ic_player)
                else -> glideRequest.load(item.avatarUrl)
            }
                .apply(RequestOptions.circleCropTransform())
                .into(userAvatarImageView)
        }
    }

    class LeaderboardViewHolder(val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root)
}