package ru.kuchanov.scpquiz.controller.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.request.RequestOptions
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.item_leaderboard.view.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LeaderboardViewModel
import ru.kuchanov.scpquiz.ui.utils.GlideApp

class LeaderboardDelegate : AbsListItemAdapterDelegate<LeaderboardViewModel, MyListItem, LeaderboardDelegate.LeaderboardViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup): LeaderboardViewHolder =
            LeaderboardDelegate.LeaderboardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false))

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int): Boolean = item is LeaderboardViewModel

    override fun onBindViewHolder(item: LeaderboardViewModel, viewHolder: LeaderboardViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView) {
            userFullNameTextView.text = item.name
            userScoreTextView.text = item.score.toString()
            userPositionTextView.text = (viewHolder.adapterPosition + 1).toString()
            userFullCompleteLevelsTextView.text = context.getString(R.string.complete_levels_text, item.fullCompleteLevels)
            userPartCompleteLevelsTextView.text = context.getString(R.string.part_complete_levels_text, item.partCompleteLevels)

            val glideRequest = GlideApp.with(userAvatarImageView.context)
            when (item.avatarUrl) {
                null -> glideRequest.load(R.drawable.ic_player)
                else -> glideRequest.load(item.avatarUrl)
            }
                    .apply(RequestOptions.circleCropTransform())
                    .into(userAvatarImageView)
        }
    }

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}