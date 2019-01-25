package ru.kuchanov.scpquiz.controller.adapter.delegate

import android.net.Uri
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
import ru.kuchanov.scpquiz.ui.utils.getImageUrl
import ru.kuchanov.scpquiz.utils.DimensionUtils
import ru.kuchanov.scpquiz.utils.StorageUtils
import timber.log.Timber


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
            @Suppress("SimplifyBooleanWithConstants")
            quizProgressView.visibility = if (item.showProgress == true) View.VISIBLE else View.GONE
            Timber.d("ITEM onBindViewHolder:%s", item)
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

    class LevelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}