package com.gamapp.searchbar

import com.google.gson.annotations.Expose
import java.util.*

class HistoryModel(
    @Expose
    var value:String,
    @Expose
    var weight:Int = 0
    ,@Expose
    var tag:String = UUID.randomUUID().toString()
    )