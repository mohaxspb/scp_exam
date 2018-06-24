package ru.kuchanov.scpquiz.controller.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import kotlinx.android.synthetic.main.list_item_level.view.*
import ru.kuchanov.scpquiz.R
import ru.kuchanov.scpquiz.controller.adapter.MyListItem
import ru.kuchanov.scpquiz.controller.adapter.viewmodel.LevelViewModel
import ru.kuchanov.scpquiz.ui.utils.GlideApp
import ru.kuchanov.scpquiz.utils.DimensionUtils


class LevelDelegate(
    private val clickListener: (Long) -> Unit
) : AbsListItemAdapterDelegate<LevelViewModel, MyListItem, LevelDelegate.LevelViewHolder>() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) = item is LevelViewModel

    override fun onCreateViewHolder(parent: ViewGroup): LevelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_level, parent, false)
        view.layoutParams.width = DimensionUtils.getScreenWidth() / 3
        return LevelViewHolder(view)
    }

    override fun onBindViewHolder(item: LevelViewModel, viewHolder: LevelViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView) {
            if (item.scpNameFilled || item.scpNumberFilled) {
                GlideApp.with(imageView.context)
                        .load(item.quiz.imageUrl)
                        .dontAnimate()
                        .centerCrop()
                        .into(imageView)
                if (item.scpNumberFilled && item.scpNameFilled) {
                    strokeView.visibility = View.GONE
                    scpNumberTextView.visibility = View.VISIBLE
                    scpNumberTextView.text = context.getString(R.string.scp_placeholder, item.quiz.scpNumber)
                } else {
                    strokeView.visibility = View.VISIBLE
                    scpNumberTextView.visibility = View.GONE
                }
            } else {
                imageView.setImageResource(R.drawable.ic_level_unknown)
                strokeView.visibility = View.VISIBLE
                scpNumberTextView.visibility = View.GONE
            }

            setOnClickListener { clickListener(item.quiz.id) }
        }
    }

    class LevelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}