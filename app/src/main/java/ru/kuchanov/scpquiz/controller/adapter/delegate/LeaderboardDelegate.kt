package ru.kuchanov.scpquiz.controller.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.item_leaderboard.view.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.UserLeaderboardViewModel
import ru.kuchanov.scpquiz.ui.utils.GlideApp

class LeaderboardDelegate : AbsListItemAdapterDelegate<UserLeaderboardViewModel, MyListItem, LeaderboardDelegate.LeaderboardViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup): LeaderboardViewHolder =
            LeaderboardDelegate.LeaderboardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false))

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int): Boolean = item is UserLeaderboardViewModel

    override fun onBindViewHolder(item: UserLeaderboardViewModel, viewHolder: LeaderboardViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView) {
            userFullNameTextView.text = item.name
            userScoreTextView.text = item.score.toString()
            userPositionTextView.text = (viewHolder.adapterPosition + 1).toString()
            GlideApp
                    .with(userAvatarImageView.context)
                    .load(item.avatarUrl)
                    .placeholder(R.drawable.ic_player)
                    .centerInside()
                    .into(userAvatarImageView)
        }
    }

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}