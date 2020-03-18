package com.scp.scpexam.controller.adapter.delegate

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AbsListItemAdapterDelegate
import com.scp.scpexam.R
import com.scp.scpexam.controller.adapter.MyListItem
import com.scp.scpexam.controller.adapter.viewmodel.LevelViewModel
import kotlinx.android.synthetic.main.list_item_level.view.*
import com.scp.scpexam.ui.utils.GlideApp
import com.scp.scpexam.ui.utils.getImageUrl
import com.scp.scpexam.utils.DimensionUtils
import com.scp.scpexam.utils.StorageUtils


class LevelDelegate(
        private val clickListener: (LevelViewModel) -> Unit,
        private val unlockLevelListener: (LevelViewModel, Int) -> Unit
) : AbsListItemAdapterDelegate<LevelViewModel, MyListItem, LevelDelegate.LevelViewHolder>() {

    override fun isForViewType(item: MyListItem, items: MutableList<MyListItem>, position: Int) = item is LevelViewModel

    override fun onCreateViewHolder(parent: ViewGroup): LevelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_level, parent, false)
        view.layoutParams.width = DimensionUtils.getScreenWidth() / 3
        return LevelViewHolder(view)
    }

    override fun onBindViewHolder(item: LevelViewModel, viewHolder: LevelViewHolder, payloads: MutableList<Any>) {
        with(viewHolder.itemView) {
            quizProgressView.visibility = if (item.showProgress) View.VISIBLE else View.GONE
            if (item.scpNameFilled || item.scpNumberFilled) {
                imageView.setPadding(0, 0, 0, 0)
                with(GlideApp.with(imageView.context)) {
                    if (StorageUtils.ifFileExistsInAssets(item.quiz.getImageUrl(), imageView.context, "quizImages")) {
                        load(Uri.parse("file:///android_asset/quizImages/${item.quiz.getImageUrl()}"))
                    } else {
                        load(item.quiz.imageUrl)
                    }
                }
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

                setOnClickListener { clickListener(item) }
            } else {
                if (item.isLevelAvailable) {
                    imageView.setPadding(0, 0, 0, 0)
                    imageView.setImageResource(R.drawable.ic_level_unknown)
                    imageView.setBackgroundResource(android.R.color.black)
                    setOnClickListener { clickListener(item) }
                } else {
                    val padding = DimensionUtils.dpToPx(5)
                    imageView.setPadding(padding, padding, padding, padding)
                    imageView.setImageResource(R.drawable.ic_coins5)
                    imageView.setBackgroundResource(R.color.backgroundColorLevelLocked)
                    setOnClickListener { unlockLevelListener(item, viewHolder.adapterPosition) }
                }
                strokeView.visibility = View.VISIBLE
                scpNumberTextView.visibility = View.GONE
            }
        }
    }

    class LevelViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}