package com.gamapp.searchbar

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.gamapp.searchbar.databinding.HistoryItemLayoutBinding
import java.lang.Math.min

class HistoryAdapter(
    var context: Context,
    var uiData: UiData? = null,
    val param: (Boolean) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryHolder>() {
    var historyManager = HistoryManager.getInstance(context)
    var offeredHistory = ArrayList<HistoryModel>()
        set(value) {
            field = value
            param(value.size != 0)
            notifyDataSetChanged()
        }

    fun getMaxSize() = uiData?.maxOfferedHistorySize ?: 4
    private var onHistoryClickListener: ((HistoryModel) -> Unit)? = null

    var searchedText = ""
    fun search(item: String) {
        searchedText = item
        offeredHistory = historyManager.searchInHistory(item)
    }

    inner class HistoryHolder(val viewBinding: HistoryItemLayoutBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        init {
            Toast.makeText(context, "i runed", Toast.LENGTH_SHORT).show()
            viewBinding.historyDeleteIcon.setOnClickListener {
                historyManager.removeItem(offeredHistory[adapterPosition])
                offeredHistory = historyManager.searchInHistory(searchedText)
            }
            viewBinding.root.setOnClickListener {
                onHistoryClickListener?.let {
                    it(offeredHistory[adapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val viewBinding =
            HistoryItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return HistoryHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        holder.viewBinding.apply {

            uiData?.uiColor?.let {
                it.thirdaryColor.let {
                    root.background = ColorDrawable(it)
                }
                it.secondaryColor.let {
                    historyItemText.setTextColor(it)
                    historyIcon.setColorFilter(it)
                    historyDeleteIcon.setColorFilter(it)
                }
                it.primaryColor.let {

                }


            }
            historyItemText.text = offeredHistory[position].value
        }
    }

    override fun getItemCount(): Int = offeredHistory.size.coerceAtMost(getMaxSize())
    fun setOnHistorySelected(function: (HistoryModel) -> Unit) {
        onHistoryClickListener = function
    }

}