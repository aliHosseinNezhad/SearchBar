package com.gamapp.searchbar

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.selects.select

class HistoryManager private constructor(context: Context) {
    companion object {
        private var instance: HistoryManager? = null
        fun getInstance(context: Context): HistoryManager {
            return instance ?: synchronized(this) {
                instance = HistoryManager(context)
                instance!!
            }
        }
    }

    private var sharedPreferencesName = "SearchFragmentHistoryManagerSharedPreference"
    private var listName = "list"
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()
    private var type = object : TypeToken<ArrayList<HistoryModel>>() {}.type
    private var gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    val changeNotifier = MutableLiveData(false)

    private fun addHistoryArray(list: ArrayList<HistoryModel>): ArrayList<HistoryModel> {
        editor.putString(listName, gson.toJson(list, type))
        editor.commit()
        changeNotifier.postValue(true)
        return list
    }

    fun readyItem(item: String): String {
        var mItem = item.trim()
        mItem = mItem.lowercase()
        mItem = mItem.replace("\\s+".toRegex(), " ")
        return mItem
    }

    fun searchInHistory(
        item: String,
        param: ((Int, ArrayList<HistoryModel>) -> Unit)? = null
    ): ArrayList<HistoryModel> {
        val searchResult = ArrayList<HistoryModel>()
        val historyArray = getHistoryArray()
        val searched = readyItem(item)
        if (searched.isEmpty())
            return searchResult
        for (i in historyArray.indices) {
            if (historyArray[i].value.contains(searched)) {
                param?.let { it(i, historyArray) }
                searchResult.add(historyArray[i])
            }
        }
        searchResult.sortByDescending {
            it.weight
        }
        return searchResult
    }

//    private fun removeBySearchInHistory(
//        item: HistoryModel,
//        maxHistorySize: Int,
//        param: ((Int, ArrayList<HistoryModel>) -> Boolean)
//    ): ArrayList<HistoryModel> {
//        val searchResult = ArrayList<HistoryModel>()
//        val historyArray = getHistoryArray()
//        val searched = readyItem(item)
//        if (searched.isEmpty())
//            return searchResult
//        var i = 0
//        while (i in historyArray.indices) {
//            if (historyArray[i].contains(searched)) {
//                if (!param(i, historyArray)) {
//                    searchResult.add(historyArray[i])
//                } else {
//                    i--
//                }
//            }
//            i++
//            if (maxHistorySize == searchResult.size)
//                return searchResult
//        }
//        return searchResult
//    }
//
//    fun removeSearchItem(
//        searchedText: HistoryModel,
//        item: HistoryModel,
//        maxHistorySize: Int
//    ): ArrayList<HistoryModel> {
//        val historyArray = removeBySearchInHistory(searchedText, maxHistorySize)
//        { i: Int, history: ArrayList<HistoryModel> ->
//            if (item == history[i]) {
//                history.removeAt(i)
//                return@removeBySearchInHistory true
//            }
//            return@removeBySearchInHistory false
//        }
//        addHistoryArray(historyArray)
//        return historyArray
//    }


    fun removeItem(item: HistoryModel): ArrayList<HistoryModel> {
        val histories = getHistoryArray()
        histories.forEach {
            if (it.tag == item.tag) {
                histories.remove(it)
                return addHistoryArray(histories)
            }
        }
        return histories
    }

    fun getHistoryArray(): ArrayList<HistoryModel> = gson.fromJson(
        sharedPreferences
            .getString(listName, "[]"), type
    )

    fun addHistoryItem(item: HistoryModel) {
        val historyArray = getHistoryArray()
        item.value = readyItem(item.value)
        historyArray.forEach {
            if (it.value == item.value) {
                it.weight++
                addHistoryArray(historyArray)
                return
            }
        }
        addHistoryArray(historyArray.apply { add(item) })
    }

    fun removeItem(index: Int): ArrayList<HistoryModel> {
        val array = getHistoryArray()
        if (index in array.indices) {
            array.removeAt(index)
            addHistoryArray(array)
        }
        return array
    }


}