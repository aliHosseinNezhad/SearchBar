package com.gamapp.searchbar

import android.graphics.Typeface

class UiData {
    class UiColor {
        var thirdaryColor: Int = 0
        var secondaryColor: Int = 0
        var primaryColor: Int = 0
    }
    var uiColor: UiColor = UiColor()
    var typeface: Typeface? = null
    var maxOfferedHistorySize = 4
}